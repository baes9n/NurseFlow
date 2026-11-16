package ec.edu.nurseflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ec.edu.nurseflow.auth.TokenRevocadoRepository;
import ec.edu.nurseflow.common.ApiError;
import ec.edu.nurseflow.usuarios.Usuario;
import ec.edu.nurseflow.usuarios.UsuarioRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    // No se usan usuarios de Spring Security generados en memoria.
    @Bean
    UserDetailsService deshabilitarUsuarioPorDefecto() {
        return username -> { throw new UsernameNotFoundException("Solo se permite autenticacion JWT"); };
    }

    @Bean
    SecretKey jwtKey(@Value("${nurseflow.auth.jwt-secret-b64}") String encoded) {
        if (encoded == null || encoded.isBlank()) {
            throw new IllegalStateException("Falta JWT_SECRET_B64. Ejecuta ./preparar-etapa02.sh y carga .env.");
        }
        try {
            byte[] raw = Base64.getDecoder().decode(encoded);
            if (raw.length < 32) throw new IllegalArgumentException("La clave JWT debe tener al menos 32 bytes");
            return new SecretKeySpec(raw, "HmacSHA256");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT_SECRET_B64 debe ser Base64 de al menos 32 bytes", e);
        }
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }
    @Bean
    JwtDecoder jwtDecoder(SecretKey key, @Value("${nurseflow.auth.issuer}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    @Bean
    org.springframework.core.convert.converter.Converter<Jwt, AbstractAuthenticationToken> jwtUserConverter(
            UsuarioRepository usuarios, TokenRevocadoRepository revocados) {
        return jwt -> {
            Long id;
            try { id = Long.valueOf(jwt.getSubject()); }
            catch (Exception ex) { throw invalidToken(); }
            Usuario user = usuarios.findById(id).orElseThrow(SecurityConfig::invalidToken);
            Number version = jwt.getClaim("version");
            String jti = jwt.getId();
            if (!Boolean.TRUE.equals(user.getActivo()) || version == null
                || version.intValue() != user.getTokenVersion() || jti == null
                || revocados.existsById(jti)) throw invalidToken();
            return new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol().name())), user.getCorreo());
        };
    }
    private static OAuth2AuthenticationException invalidToken() {
        return new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Token invalido o revocado", null));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            org.springframework.core.convert.converter.Converter<Jwt, AbstractAuthenticationToken> jwtUserConverter,
            AuthenticationEntryPoint unauthorized, AccessDeniedHandler forbidden) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(b -> b.disable()).formLogin(f -> f.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/sistema/estado", "/actuator/health", "/v3/api-docs", "/v3/api-docs/**",
                    "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jwtUserConverter))
                .authenticationEntryPoint(unauthorized))
            .exceptionHandling(e -> e.authenticationEntryPoint(unauthorized).accessDeniedHandler(forbidden))
            .build();
    }

    @Bean
    AuthenticationEntryPoint unauthorized(ObjectMapper mapper) {
        return (request, response, ex) -> error(mapper, response, request.getRequestURI(),
            HttpStatus.UNAUTHORIZED, "Inicia sesion con un token valido");
    }
    @Bean
    AccessDeniedHandler forbidden(ObjectMapper mapper) {
        return (request, response, ex) -> error(mapper, response, request.getRequestURI(),
            HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta operacion");
    }
    private void error(ObjectMapper mapper, jakarta.servlet.http.HttpServletResponse response,
            String path, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), new ApiError(Instant.now(), status.value(),
            status.getReasonPhrase(), message, path, Map.of()));
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${nurseflow.cors.allowed-origins}") String origins) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim)
            .filter(x -> !x.isEmpty()).toList());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setExposedHeaders(List.of("Location"));
        cors.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}

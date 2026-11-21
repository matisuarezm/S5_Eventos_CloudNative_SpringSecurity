package cl.duoc.eventos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Semana 5: seguridad del microservicio con Spring Security.
 *
 * El servicio funciona como "resource server" OAuth2: no tiene usuarios ni contrasenas propias,
 * solo acepta peticiones que traen un token JWT valido emitido por el IDaaS (Azure AD B2C).
 *
 * Por cada peticion, el filtro BearerTokenAuthenticationFilter lee el header
 * "Authorization: Bearer <token>" y valida:
 *   - la firma, con las claves publicas del emisor (se descargan desde el issuer-uri),
 *   - el emisor (claim iss) = spring.security.oauth2.resourceserver.jwt.issuer-uri,
 *   - la audiencia (claim aud) = spring.security.oauth2.resourceserver.jwt.audiences,
 *   - que no este vencido (claim exp).
 * Si falta el token o no es valido, responde 401 antes de llegar al controller.
 *
 * Asi la API queda protegida dos veces: en AWS API Gateway (autorizador JWT, Semana 4)
 * y aqui, aunque alguien llame directo a la IP de la EC2 sin pasar por el Gateway.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API REST sin formularios ni cookies: CSRF no aplica
                .csrf(csrf -> csrf.disable())
                // Sin sesion: cada peticion debe traer su propio token (stateless)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Healthcheck de Docker Compose y del pipeline: debe responder sin token
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        // Todo lo demas exige un JWT valido
                        .anyRequest().authenticated())
                // Valida el JWT con la configuracion de application.properties (issuer-uri y audiences)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}

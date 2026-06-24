package com.aivle.bookapp.config;

import com.aivle.bookapp.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//CORS 세부설정 추가를 위한 수정
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@RequiredArgsConstructor

public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(exception -> exception.authenticationEntryPoint(
                        (request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"message\":\"인증이 필요합니다.\"}");
                        }
                ))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/register", "/users/login", "/users/refresh").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books", "/books/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/books").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/books").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/books/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/books/*/comments").permitAll()
                        .requestMatchers(HttpMethod.POST, "/books/*/comments").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/books/*/comments/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/books/*/comments/*/like").authenticated()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
    //CORS 세부설정 수정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://http://aivle05-book-frontend-bukkit.s3-website-us-east-1.amazonaws.com" 
        ));
        // 프론트엔드가 사용할 수 있는 HTTP 메서드 허용
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // JWT 토큰 등을 주고받기 위해 필수

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

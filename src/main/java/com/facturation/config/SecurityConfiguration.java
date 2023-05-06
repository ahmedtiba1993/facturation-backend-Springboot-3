package com.facturation.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf()
        .disable()
        .authorizeHttpRequests()
            .requestMatchers("/facturation/auth/**",
              "/v2/api-docs",
              "/v3/api-docs",
              "/v3/api-docs/**",
              "/swagger-resources",
              "/swagger-resources/**",
              "/configuration/ui",
              "/configuration/security",
              "/swagger-ui/**",
              "/webjars/**",
              "/swagger-ui.html")
      .permitAll()
            .anyRequest()
        .authenticated()
            .and().cors().configurationSource(request-> {
              CorsConfiguration configuration = new CorsConfiguration();
              //configuration.setAllowedOrigins(Arrays.asList("http://aalarme.s3-website.eu-west-3.amazonaws.com/"));
              configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200/"));
              configuration.setAllowedMethods(Arrays.asList("*"));
              configuration.setAllowedHeaders(List.of("*"));
              return configuration;
            })
            .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}

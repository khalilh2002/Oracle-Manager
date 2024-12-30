package com.lsi.oracle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();

    // Specify allowed origins using patterns
    config.addAllowedOriginPattern("*"); // For Spring Boot 2.4+ use origin patterns
    config.addAllowedHeader("*");       // Allow all headers
    config.addAllowedMethod("*");       // Allow all HTTP methods
    config.setAllowCredentials(true);   // Allow credentials

    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}

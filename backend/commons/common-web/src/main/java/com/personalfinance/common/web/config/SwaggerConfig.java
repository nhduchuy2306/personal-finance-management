package com.personalfinance.common.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 * JWT bearer authentication scheme.
 */
@Configuration
public class SwaggerConfig {

  @Value("${spring.application.name:personal-finance}")
  private String appName;

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
      .info(new Info()
        .title(appName + " API")
        .version("1.0.0")
        .description("Personal Finance Manager — " + appName))
      .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
      .schemaRequirement("bearerAuth", new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT"));
  }
}

package com.user.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("User APIs")
						.description("Comprehensive API documentation")
						.version("1.0"))
				.servers(List.of(
						new Server().url("http://localhost:8086").description("Local Environment"),
						new Server().url("https://user-service-2tqh.onrender.com").description("Production Environment")
				));
	}

}

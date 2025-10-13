package com.compiler.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI codeCompilerOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Code Compiler API")
                        .description("A multi-language code execution and compilation service supporting " +
                                "Java, Python, C++, JavaScript, and more.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Code Compiler Team")
                                .email("support@compiler.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

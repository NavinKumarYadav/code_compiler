package com.compiler.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class SecurityHeadersConfig {

    @Bean
    public FilterRegistrationBean<HeaderFilter> securityHeadersFilter() {
        FilterRegistrationBean<HeaderFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HeaderFilter());
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }

    public static class HeaderFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                             FilterChain chain) throws IOException, ServletException {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
            httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000");
            chain.doFilter(request, response);
        }
    }
}
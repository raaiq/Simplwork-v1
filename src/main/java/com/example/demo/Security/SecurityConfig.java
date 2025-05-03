package com.example.demo.Security;

import com.example.demo.Filters.ActionTokenAuthFilter;
import com.example.demo.Filters.ActionTokenAuthenticationProvider;
import com.example.demo.Repositories.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

//TODO:Have separate package for configurations
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${auth0.audience}")
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Autowired
    private Environment environment;
    @Value("${custom.security.allowed-origins:}")
    private String allowedOrigins;

    //TODO:Have common filter chain but different authentication mechanisms based on type
    @Bean
    @Order(1)
    protected SecurityFilterChain configActionTokenSecurity(HttpSecurity http,ActionTokenAuthenticationProvider actionTokenProvider) throws  Exception{
        http.securityMatcher("/api/actionToken/**");
        http.authorizeHttpRequests().requestMatchers("/*.html","/*.svg","/*.png","/_next/**","/e/**","/*").permitAll().anyRequest().authenticated().and().
        addFilterAfter(new ActionTokenAuthFilter(actionTokenProvider), LogoutFilter.class);
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().csrf().disable();
        http.cors(Customizer.withDefaults());
        return http.build();
    }

    @Order(2)
    @Bean
    protected SecurityFilterChain configHTTPSecurity(HttpSecurity http, UserRepo userRepo) throws Exception {
        http.authorizeHttpRequests().requestMatchers("/*.html","/*.svg","/*.png","/_next/**","/e/**","/*").permitAll().anyRequest().fullyAuthenticated().and().
                oauth2ResourceServer().jwt();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().csrf().disable();

        http.addFilterAfter(new RegisterOAuthUserFilter(userRepo), BearerTokenAuthenticationFilter.class);
        http.cors(Customizer.withDefaults());

        return http.build();
    }
    //TODO: Remove cors policy in production
    //TODO: Check if removing EnableWebMVC disables CORS
    @Configuration
    public static class WebConfig implements WebMvcConfigurer{

        @Value("${custom.security.allowed-origins:}")
        private String allowedOrigins;
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            if(allowedOrigins ==null | Objects.equals(allowedOrigins, "")){
                return;
            }
            registry.addMapping("/**")
                    .allowedOrigins(allowedOrigins.split(" "))
                    .allowedMethods(HttpMethod.DELETE.name(),
                                    HttpMethod.GET.name(),
                                    HttpMethod.HEAD.name(),
                                    HttpMethod.PATCH.name(),
                                    HttpMethod.POST.name(),
                                    HttpMethod.OPTIONS.name());
        }
    }

    @Bean
    protected WebSecurityCustomizer configWebSecurity(){
        return this::customizeWebSecurity;
    }

    private void customizeWebSecurity(WebSecurity web) {
        if(Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            web.ignoring().requestMatchers(new AntPathRequestMatcher("/h2-console/**"));
            web.ignoring().requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**");
        }
    }


    //For 0Auth2 authentication
    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }


    @Bean
    SecureRandom createSecureRandom() throws NoSuchAlgorithmException {
        return SecureRandom.getInstanceStrong();
    }
}

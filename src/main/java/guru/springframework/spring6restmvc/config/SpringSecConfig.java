package guru.springframework.spring6restmvc.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Created by jt, Spring Framework Guru.
 */
@Profile("!test") //I don't want this config to be applied when I have this profile
@Configuration
public class SpringSecConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {

        http.securityMatcher(EndpointRequest.toAnyEndpoint())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

//        http.authorizeHttpRequests(authorize -> authorize.requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()); Alternative

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/v3/api-docs**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .anyRequest().authenticated();
                })
                .oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer -> {
                    httpSecurityOAuth2ResourceServerConfigurer.jwt(Customizer.withDefaults());
                });

        return http.build();
    }

}
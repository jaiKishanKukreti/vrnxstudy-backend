package com.vrnxstudy.vrnxstudy_api.config;


import com.vrnxstudy.vrnxstudy_api.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider provider= new DaoAuthenticationProvider(customUserDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager( AuthenticationConfiguration configuation) throws Exception{
        return configuation.getAuthenticationManager();
    }


    @Bean public SecurityContextRepository securityContextRepository(){
        return new HttpSessionSecurityContextRepository();
    }

//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http, DaoAuthenticationProvider provider, SecurityContextRepository contextRepository) throws Exception {
//        http.csrf(csrf ->csrf.disable())
//                .authenticationProvider(provider)
//                .securityContext(context->
//                        context.securityContextRepository(contextRepository))
//                .sessionManagement(session->
//                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//                .authorizeHttpRequests(auth-> auth
//                        .requestMatchers("/register").permitAll()
//                        .requestMatchers("/login").permitAll()
//                        .requestMatchers("/student/**").permitAll()
//
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .anyRequest().authenticated())
//                .formLogin(form->form.disable())
//                .httpBasic(basic-> basic.disable());
//        return http.build();
//    }
@Bean
public RoleHierarchy roleHierarchy() {
    // SUPER_ADMIN > ADMIN > STUDENT
    return RoleHierarchyImpl.fromHierarchy(
            "ROLE_SUPER_ADMIN > ROLE_ADMIN\n" +
                    "ROLE_ADMIN > ROLE_STUDENT"
    );
}
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception{
        http
                .cors(Customizer.withDefaults())
                .csrf((csrf)->csrf.disable())
                .sessionManagement(session->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth->
                        auth.requestMatchers("/auth/register","/auth/login").permitAll()

                                .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2->
                        oauth2.jwt(jwt->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }


    // secret Key generation:
    @Bean
    public SecretKey secretKey(@Value("${jwt.secret}") String secret){
        byte[] decodedKey= Base64.getDecoder().decode(secret);
        return new SecretKeySpec(decodedKey,"HmacSHA256");
    }


    // JWt encoder
    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey){
        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey, @Value("${jwt.issuer}") String issuer){

        NimbusJwtDecoder decoder= NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();


        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuer)
        );
        return decoder;
    }


    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter authoritiesConverter=
                new JwtGrantedAuthoritiesConverter();
        authoritiesConverter
                .setAuthoritiesClaimName("authorities");

        authoritiesConverter
                .setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter= new JwtAuthenticationConverter();

        authenticationConverter
                .setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }
}

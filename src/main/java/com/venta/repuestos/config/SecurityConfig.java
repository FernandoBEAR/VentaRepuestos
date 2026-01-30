package com.venta.repuestos.config;

import com.venta.repuestos.config.filter.JwtTokenValidator;
import com.venta.repuestos.servicios.Impl.UserDetailServiceImp;
import com.venta.repuestos.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity //Utilizar anotacion de spring security
public class SecurityConfig {

    @Autowired
    private JwtUtils jwtUtils;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authProvider) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> {
                    /* ───────────────────────────────
                       ENDPOINTS PÚBLICOS
                    ─────────────────────────────── */
                    auth.requestMatchers(HttpMethod.POST, "/auth/**").permitAll();

                    /* ───────────────────────────────
                       CLIENTES
                    ─────────────────────────────── */
                    auth.requestMatchers(HttpMethod.GET, "/api/v1/clientes/**")
                            .hasAuthority("CLIENTE_READ");
                    auth.requestMatchers(HttpMethod.POST, "/api/v1/clientes/**")
                            .hasAuthority("CLIENTE_WRITE");
                    auth.requestMatchers(HttpMethod.PUT, "/api/v1/clientes/**")
                            .hasAuthority("CLIENTE_WRITE");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/**")
                            .hasAuthority("CLIENTE_DELETE");

                    /* ───────────────────────────────
                       REPUESTOS
                    ─────────────────────────────── */
                    auth.requestMatchers(HttpMethod.GET, "/api/repuestos/**")
                            .hasAuthority("REPUESTO_READ");

                    auth.requestMatchers(HttpMethod.POST, "/api/repuestos/**")
                            .hasAuthority("REPUESTO_WRITE");

                    auth.requestMatchers(HttpMethod.PUT, "/api/repuestos/**")
                            .hasAuthority("REPUESTO_WRITE");

                    auth.requestMatchers(HttpMethod.DELETE, "/api/repuestos/**")
                            .hasAuthority("REPUESTO_DELETE");

                    // Aumentar/reducir stock manual
                    auth.requestMatchers(HttpMethod.PUT, "/api/repuestos//aumentar-stock/*")
                            .hasAuthority("REPUESTO_STOCK_MANUAL");

                    auth.requestMatchers(HttpMethod.PUT, "/api/repuestos//reducir-stock/*")
                            .hasAuthority("REPUESTO_STOCK_MANUAL");

                    /* ───────────────────────────────
                       VENTAS
                    ─────────────────────────────── */
                    auth.requestMatchers(HttpMethod.GET, "/api/ventas/**")
                            .hasAuthority("VENTA_READ");

                    auth.requestMatchers(HttpMethod.POST, "/api/ventas/**")
                            .hasAuthority("VENTA_WRITE");

                    auth.requestMatchers(HttpMethod.PUT, "/api/ventas/**")
                            .hasAuthority("VENTA_WRITE");

                    auth.requestMatchers(HttpMethod.DELETE, "/api/ventas/**")
                            .hasAuthority("VENTA_DELETE");

                    /* ───────────────────────────────
                       RESTO DE ENDPOINTS NO PERMITIDOS
                    ─────────────────────────────── */
                    auth.anyRequest().permitAll();
                })

                .authenticationProvider(authProvider)
                .addFilterBefore(new JwtTokenValidator(jwtUtils), BasicAuthenticationFilter.class)
                .build();
    }

    //Gestionar las autenticaciones
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailServiceImp userDetailServiceImp) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailServiceImp);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        //Contraseña sin encriptar
        //return NoOpPasswordEncoder.getInstance();

        //Contraseña Encriptada
        return new BCryptPasswordEncoder();
    }


}

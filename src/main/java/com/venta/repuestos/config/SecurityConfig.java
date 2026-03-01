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
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @Autowired
        private JwtUtils jwtUtils;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authProvider)
                        throws Exception {

                return http
                                // Deshabilitamos CSRF ya que usamos JWT (stateless)
                                .csrf(csrf -> csrf.disable())
                                .httpBasic(Customizer.withDefaults())
                                // Configuración stateless para JWT (sin sesiones del lado del servidor)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .authorizeHttpRequests(auth -> {

                                        // ENDPOINTS PÚBLICOS - Sin autenticación requerida

                                        auth.requestMatchers(HttpMethod.POST, "/auth/**").permitAll();
                                        auth.requestMatchers(HttpMethod.GET, "/auth/**").permitAll();

                                        // ENDPOINTS SEMÁNTICOS (Web Semántica / SPARQL) - Acceso público
                                        auth.requestMatchers("/api/semantico/**").permitAll();

                                        // GESTIÓN DE USUARIOS - Solo ADMIN
                                        // Principio: El administrador tiene control total sobre usuarios

                                        auth.requestMatchers(HttpMethod.GET, "/api/users/**")
                                                        .hasAnyRole("ADMIN");
                                        auth.requestMatchers(HttpMethod.POST, "/api/users/**")
                                                        .hasAnyRole("ADMIN");
                                        auth.requestMatchers(HttpMethod.PUT, "/api/users/**")
                                                        .hasAnyRole("ADMIN");
                                        auth.requestMatchers(HttpMethod.DELETE, "/api/users/**")
                                                        .hasAnyRole("ADMIN");

                                        // CLIENTES
                                        // - ADMIN: CRUD completo
                                        // - VENDEDOR: Crear y actualizar (necesario para registrar ventas)

                                        // Lectura: ADMIN puede leer cualquier cliente
                                        auth.requestMatchers(HttpMethod.GET, "/api/v1/clientes/**")
                                                        .hasAnyRole("ADMIN", "VENDEDOR");

                                        // Creación: ADMIN y VENDEDOR pueden crear clientes
                                        auth.requestMatchers(HttpMethod.POST, "/api/v1/clientes/**")
                                                        .hasAnyRole("ADMIN", "VENDEDOR");

                                        // Actualización: ADMIN y VENDEDOR pueden actualizar clientes
                                        auth.requestMatchers(HttpMethod.PUT, "/api/v1/clientes/**")
                                                        .hasAnyRole("ADMIN", "VENDEDOR");

                                        // Eliminación: Solo ADMIN puede eliminar clientes
                                        auth.requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/**")
                                                        .hasRole("ADMIN");

                                        // REPUESTOS
                                        // - ADMIN: CRUD completo
                                        // - LOGISTICA: Crear, editar, controlar stock, leer
                                        // - VENDEDOR: Solo lectura (para consultar disponibilidad)

                                        // Lectura: Todos los roles pueden consultar repuestos
                                        auth.requestMatchers(HttpMethod.GET, "/api/repuestos/**")
                                                        .hasAnyRole("ADMIN", "VENDEDOR", "LOGISTICA");

                                        // Creación: Solo ADMIN y LOGISTICA pueden crear repuestos
                                        auth.requestMatchers(HttpMethod.POST, "/api/repuestos/**")
                                                        .hasAnyRole("ADMIN", "LOGISTICA");

                                        // Actualización general: ADMIN y LOGISTICA
                                        auth.requestMatchers(HttpMethod.PUT, "/api/repuestos/**")
                                                        .hasAnyRole("ADMIN", "LOGISTICA");

                                        // Control de stock manual: ADMIN y LOGISTICA
                                        auth.requestMatchers(HttpMethod.PUT, "/api/repuestos/*/aumentar-stock/*")
                                                        .hasAnyRole("ADMIN", "LOGISTICA");
                                        auth.requestMatchers(HttpMethod.PUT, "/api/repuestos/*/reducir-stock/*")
                                                        .hasAnyRole("ADMIN", "LOGISTICA");

                                        // Eliminación: Solo ADMIN puede eliminar repuestos
                                        auth.requestMatchers(HttpMethod.DELETE, "/api/repuestos/**")
                                                        .hasRole("ADMIN");

                                        // VENTAS
                                        // - ADMIN: CRUD completo (gestión total de ventas)
                                        // - VENDEDOR: Crear y leer ventas

                                        // Lectura: ADMIN y VENDEDOR pueden ver ventas
                                        auth.requestMatchers(HttpMethod.GET, "/api/ventas/**")
                                                        .hasAnyRole("ADMIN", "VENDEDOR");

                                        // Creación: ADMIN y VENDEDOR pueden crear ventas
                                        auth.requestMatchers(HttpMethod.POST, "/api/ventas/**")
                                                        .hasAnyRole("ADMIN", "VENDEDOR");

                                        auth.requestMatchers(HttpMethod.PUT, "/api/ventas/*/entregar-productos")
                                                        .hasAnyRole("ADMIN", "LOGISTICA");
                                        // Actualización: Solo ADMIN puede modificar ventas existentes
                                        auth.requestMatchers(HttpMethod.PUT, "/api/ventas/**")
                                                        .hasRole("ADMIN");

                                        // Eliminación/Anulación: Solo ADMIN puede anular ventas
                                        auth.requestMatchers(HttpMethod.DELETE, "/api/ventas/**")
                                                        .hasRole("ADMIN");

                                        // DETALLE DE VENTAS
                                        // - ADMIN: CRUD completo
                                        // - VENDEDOR: Crear y leer detalles (asociados a sus ventas)

                                        auth.requestMatchers(HttpMethod.GET, "/api/detalles-venta/**")
                                                        .hasAnyRole("ADMIN", "VENDEDOR");

                                        auth.requestMatchers(HttpMethod.POST, "/api/detalles-venta/**")
                                                        .hasAnyRole("ADMIN", "VENDEDOR");

                                        auth.requestMatchers(HttpMethod.PUT, "/api/detalles-venta/**")
                                                        .hasRole("ADMIN");

                                        auth.requestMatchers(HttpMethod.DELETE, "/api/detalles-venta/**")
                                                        .hasRole("ADMIN");

                                        // PAGOS
                                        // - ADMIN: CRUD completo
                                        // - VENDEDOR: Crear y leer pagos

                                        // Lectura: ADMIN y VENDEDOR pueden ver pagos
                                        auth.requestMatchers(HttpMethod.GET, "/api/v1/pagos/**")
                                                        .hasAnyRole("ADMIN", "VENDEDOR");

                                        // Creación: ADMIN y VENDEDOR pueden registrar pagos
                                        auth.requestMatchers(HttpMethod.POST, "/api/v1/pagos/**")
                                                        .hasAnyRole("ADMIN", "VENDEDOR");

                                        // Actualización: Solo ADMIN puede modificar pagos
                                        auth.requestMatchers(HttpMethod.PUT, "/api/v1/pagos/**")
                                                        .hasAnyRole("ADMIN", "VENDEDOR");

                                        // Eliminación: Solo ADMIN puede eliminar pagos
                                        auth.requestMatchers(HttpMethod.DELETE, "/api/v1/pagos/**")
                                                        .hasRole("ADMIN");

                                    auth.requestMatchers(HttpMethod.GET, "/api/semantico/**")
                                                    .permitAll();

                                    // Creación: ADMIN y VENDEDOR pueden registrar pagos
                                    auth.requestMatchers(HttpMethod.POST, "/api/semantico/**")
                                            .permitAll();

                                    // Actualización: Solo ADMIN puede modificar pagos
                                    auth.requestMatchers(HttpMethod.PUT, "/api/semantico/**")
                                            .permitAll();

                                    // Eliminación: Solo ADMIN puede eliminar pagos
                                    auth.requestMatchers(HttpMethod.DELETE, "/api/semantico/**")
                                            .permitAll();

                                        auth.anyRequest().denyAll();
                                })

                                .authenticationProvider(authProvider)
                                .addFilterBefore(new JwtTokenValidator(jwtUtils), BasicAuthenticationFilter.class)
                                .build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
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
                return new BCryptPasswordEncoder();
        }

}

package com.logifin.config;

import com.logifin.security.CustomUserDetailsService;
import com.logifin.security.JwtAuthenticationEntryPoint;
import com.logifin.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(unauthorizedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // Public endpoints
                .antMatchers("/api/v1/auth/**").permitAll()
                .antMatchers("/api/v1/health/**").permitAll()
                .antMatchers("/api/v1/info").permitAll()
                .antMatchers("/actuator/**").permitAll()
                // Swagger UI endpoints
                .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                // Role-based access for user management
                .antMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("CSR", "ADMIN", "SUPER_ADMIN")
                .antMatchers(HttpMethod.POST, "/api/v1/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .antMatchers(HttpMethod.PATCH, "/api/v1/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("SUPER_ADMIN")
                // User role assignment - Admin and Super Admin only
                .antMatchers(HttpMethod.POST, "/api/v1/user/set-role").hasAnyRole("ADMIN", "SUPER_ADMIN")
                // Role management - Super Admin only
                .antMatchers("/api/v1/roles/**").hasRole("SUPER_ADMIN")
                // All other endpoints require authentication
                .anyRequest().authenticated();

        // Add JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}

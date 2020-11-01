package com.derteuffel.school.security;

import com.derteuffel.school.services.CompteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Created by user on 23/03/2020.
 */
@Configuration
@Order(7)
public class SecretaireSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CompteService compteService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http

                .antMatcher("/secretaire/**").authorizeRequests()
                .antMatchers("/upload-dir/**","/experts/**","/files/**").permitAll()
                .antMatchers("/secretaire/**").access("hasAnyRole('ROLE_ROOT','ROLE_SECRETAIRE')")
                .and()
                .formLogin()
                .loginPage("/secretaire/login")
                .loginProcessingUrl("/secretaire/login/process")
                .defaultSuccessUrl("/secretaire/home")
                .permitAll()
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .and()
                .logout().logoutUrl("/secretaire/logout")
                .logoutSuccessUrl("/secretaire/login?logout")
                .and()
                .exceptionHandling().accessDeniedPage("/secretaire/access-denied");
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private DaoAuthenticationProvider authenticationProvider;

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/js/**",
                        "/css/**",
                        "/img/**",
                        "/vendor/**",
                        "/fonts/**",
                        "/downloadFile/**",
                        "/upload-dir/**",
                        "/images/**",
                        "/static/**",
                        "/ecole/connexion",
                        "/secretaire/registration",
                        "/secretaire/registration/**",
                        "/password/**",
                        "/ecole/save",
                        "/chat**",
                        "/topic/**",
                        "/app**",
                        "/home/**"
                        );
    }
}

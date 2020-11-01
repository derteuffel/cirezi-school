package com.derteuffel.school.security;

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
@Order(4)
public class EncadrementSecurityConfig extends WebSecurityConfigurerAdapter{


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/encadrements/**").authorizeRequests()
                .antMatchers("/upload-dir/**","/experts/**","/files/**").permitAll()
                .antMatchers("/encadrements/**").access("hasAnyRole('ROLE_ROOT','ROLE_ENCADREUR','ROLE_ENFANT')")
                .and()
                .formLogin()
                .loginPage("/encadrements/login")
                .loginProcessingUrl("/encadrements/login/process")
                .defaultSuccessUrl("/encadrements/activation/form")
                .permitAll()
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .and()
                .logout().logoutUrl("/encadrements/logout")
                .logoutSuccessUrl("/encadrements/login?logout")
                .and()
                .exceptionHandling().accessDeniedPage("/encadrements/access-denied");

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
                        "/downloadFile/**",
                        "/img/**",
                        "/vendor/**",
                        "/upload-dir/**",
                        "/fonts/**",
                        "/images/**",
                        "/home/**",
                        "/encadrements/registration**",
                        "/static/**");
    }

}

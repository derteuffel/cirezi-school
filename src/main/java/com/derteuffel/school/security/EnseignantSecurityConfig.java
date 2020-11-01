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
@Order(2)
public class EnseignantSecurityConfig extends WebSecurityConfigurerAdapter{


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/enseignant/**").authorizeRequests()
                .antMatchers("/upload-dir/**","/experts/**","/files/**").permitAll()
                .antMatchers("/enseignant/**").access("hasAnyRole('ROLE_ROOT','ROLE_ENSEIGNANT')")
                .and()
                .formLogin()
                .loginPage("/enseignant/login")
                .loginProcessingUrl("/enseignant/login/process")
                .defaultSuccessUrl("/enseignant/home")
                .permitAll()
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .and()
                .logout().logoutUrl("/enseignant/logout")
                .logoutSuccessUrl("/enseignant/login?logout")
                .and()
                .exceptionHandling().accessDeniedPage("/enseignant/access-denied");

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
                        "/fonts/**",
                        "/upload-dir/**",
                        "/images/**",
                        "/home/**",
                        "/static/**");
    }

}

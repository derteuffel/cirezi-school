package com.derteuffel.school.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Created by user on 23/03/2020.
 */
@Configuration
@Order(3)
public class ParentSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/parent/**").authorizeRequests()
                .antMatchers("/upload-dir/**","/experts/**","/files/**").permitAll()
                .antMatchers("/parent/**").access("hasAnyRole('ROLE_ROOT','ROLE_PARENT')")
                .antMatchers(
                        "/js/**",
                        "/css/**",
                        "/img/**",
                        "/downloadFile/**",
                        "/upload-dir/**",
                        "/vendor/**",
                        "/fonts/**",
                        "/home/**",
                        "/images/**",
                        "/static/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/parent/login")
                .loginProcessingUrl("/parent/login/process")
                .defaultSuccessUrl("/parent/home")
                .permitAll()
                .and()
                .logout()
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .and()
                .logout().logoutUrl("/direction/logout")
                .logoutSuccessUrl("/parent/login?logout")
                .and()
                .exceptionHandling().accessDeniedPage("/parent/access-denied");
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private DaoAuthenticationProvider authenticationProvider;

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider);
    }
}

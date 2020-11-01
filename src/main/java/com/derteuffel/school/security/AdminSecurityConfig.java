package com.derteuffel.school.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Created by user on 22/03/2020.
 */
@Configuration
@Order(5)
public class AdminSecurityConfig extends WebSecurityConfigurerAdapter{



        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/admin/**").authorizeRequests()
                    .antMatchers("/upload-dir/**","/experts/**").permitAll()
                    .antMatchers("/admin/**").access("hasAnyRole('ROLE_ADMIN','ROLE_ROOT')")
                    .and()
                    .formLogin()
                    .loginPage("/admin/login")
                    .loginProcessingUrl("/admin/login/process")
                    .defaultSuccessUrl("/admin/bibliotheque/lists")
                    .permitAll()
                    .and()
                    .logout()
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .and()
                    .logout().logoutUrl("/admin/logout")
                    .logoutSuccessUrl("/admin/login?logout")
                    .and()
                    .exceptionHandling().accessDeniedPage("/admin/access-denied");
        }


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
                        "/downloadFile/**",
                        "/upload-dir/**",
                        "/vendor/**",
                        "/fonts/**",
                        "/home/**",
                        "/images/**",
                        "/static/**");
    }

}

package com.practice.shared_payment_backend.configuration;

import com.practice.shared_payment_backend.configuration.auth.ApiBasicAuthenticationEntryPoint;
import com.practice.shared_payment_backend.configuration.auth.CustomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static java.util.Objects.requireNonNull;
import static org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private ApiBasicAuthenticationEntryPoint entryPoint;

    @Autowired
    private Environment environment;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(requireNonNull(environment.getProperty("basic.auth.username")))
                .password(createDelegatingPasswordEncoder().encode(requireNonNull(environment.getProperty("basic.auth.password"))))
                .authorities("ROLE_USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /* TODO: CSRF DISABLED DUE TO LIMITATIONS OF TESTING. THIS SHOULD NOT BE DONE IN PRODUCTION */
        http.csrf().disable();
        http.cors();

        http.authorizeRequests()
                .antMatchers("/securityNone")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
                .authenticationEntryPoint(entryPoint);

        http.addFilterAfter(new CustomFilter(), BasicAuthenticationFilter.class);
    }
}

package com.redhat.solutions.fsi.samples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration("kieServerSecurity")
@EnableWebSecurity
public class DefaultWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Log logger = LogFactory.getLog(DefaultWebSecurityConfig.class);

    @Override
    protected void configure(HttpSecurity http) throws Exception {
		http
        .csrf().disable()
        .authorizeRequests()
            .anyRequest().authenticated()
            .and()
        .httpBasic();

        logger.info("Initialized kieServerSecurity");
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password("asdfasdf").roles("kie-server","ACTUATOR");
        auth.inMemoryAuthentication().withUser("kieserver").password("kieserver1!").roles("kie-server");
        auth.inMemoryAuthentication().withUser("jack").password("asdfasdf").roles("kie-server", "HR");
        auth.inMemoryAuthentication().withUser("jill").password("asdfasdf").roles("kie-server", "PM");

        logger.info("Configured default identities");
    }
}

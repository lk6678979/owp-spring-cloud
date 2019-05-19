package com.owp.zuul;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public LoginFilter getLoginFilter(){
        return new LoginFilter();
    }
}


//package com.owp.configclient;
//
//import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
//import org.springframework.boot.web.servlet.ServletRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configurationa
//public class HystrixConfig {
//    @Bean
//    public ServletRegistrationBean getServlet(){
//
//        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
//
//        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
//
//        registrationBean.setLoadOnStartup(1);
//
//        registrationBean.addUrlMappings("/actuator/hystrix.stream");
//
//        registrationBean.setName("HystrixMetricsStreamServlet");
//
//
//        return registrationBean;
//    }
//}

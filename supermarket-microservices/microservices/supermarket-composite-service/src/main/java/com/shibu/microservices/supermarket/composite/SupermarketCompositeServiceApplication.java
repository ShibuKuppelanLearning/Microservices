package com.shibu.microservices.supermarket.composite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = {"com.shibu.microservices", "com.shibu.supermarket"})
public class SupermarketCompositeServiceApplication {
    @Bean
    public RestTemplate restTemplate() {
    	return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(SupermarketCompositeServiceApplication.class, args);
    }
}
package com.kinopio.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class Order801Application {

    public static void main(String[] args) {
        SpringApplication.run(Order801Application.class, args);
    }

}

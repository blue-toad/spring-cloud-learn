package com.kinopio.sleuth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class sleuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(sleuthApplication.class,args);
    }
}

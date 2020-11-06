package com.kinopio.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients //启动openfeign
public class FeignMain {
    public static void main(String args[]){
        SpringApplication.run(FeignMain.class, args);
    }
}

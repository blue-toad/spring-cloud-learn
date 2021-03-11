package com.kinopio.springcloud;

import com.kinopio.springcloud.config.StreamConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;

@SpringBootApplication
@EnableBinding(StreamConfig.class)
public class MyStreamApplication8801{
    public static void main(String[] args) {
        SpringApplication.run(MyStreamApplication8801.class, args);
    }
}

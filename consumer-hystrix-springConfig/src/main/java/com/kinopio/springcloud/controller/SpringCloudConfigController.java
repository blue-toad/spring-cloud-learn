package com.kinopio.springcloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class SpringCloudConfigController {

    // 从config-center上获得的参数
    @Value("${config.info}")
    private String info;

    @RequestMapping("/configTest")
    public String getInfo(){
        return info;
    }

}

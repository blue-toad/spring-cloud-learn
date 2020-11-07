package com.kinopio.springcloud.controller;

import com.kinopio.springcloud.entity.CommonResult;
import com.kinopio.springcloud.entity.Payment;
import com.kinopio.springcloud.service.PaymentInfoService;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@DefaultProperties(defaultFallback = "hystrixGlobalHandler")
public class HystrixController {

    @Autowired
    private PaymentInfoService service;

    @GetMapping("/consumer/payment/get/{id}")
//    @HystrixCommand(fallbackMethod = "hystrixHandler", commandProperties = {
//            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "800")
//    })
    @HystrixCommand
    public CommonResult<Payment> hystrixGet(@PathVariable Long id){
        log.info("访问");
        return service.getPaymentById(id);
    }
//    部分
//    public CommonResult<Payment> hystrixHandler(@PathVariable Long id){
//        CommonResult<Payment> result = new CommonResult<Payment>(1,"系统错误或繁忙请稍后再试：方法fallback");
//        return result;
//    }

//    全局
    public CommonResult<Payment> hystrixGlobalHandler(){
        CommonResult<Payment> result = new CommonResult<Payment>(1,"系统错误或繁忙请稍后再试：默认的fallback");
        return result;
    }
    // 注释部分可以为单个方法提供fallback
    // 现在使用的是全局fallback   有两种
    // 第一种使用 hystrix中的
    // @DefaultProperties(defaultFallback = "hystrixGlobalHandler")
    // @HystrixCommand
    // 第二种使用frign中内置的hystrix
    // 在service层中实现接口来订制fallback方法
    // 感觉这个不是很好 使用第一种方法
}

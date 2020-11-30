package com.kinopio.springcloud.controller;

import com.kinopio.springcloud.entity.CommonResult;
import com.kinopio.springcloud.entity.Payment;
import com.kinopio.springcloud.service.PaymentInfoService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// 熔断测试
@RestController
@Slf4j
public class HystrixTestController {

    @GetMapping("/consumer/hystrix/test/{id}")
    @HystrixCommand(fallbackMethod = "hystrixTestFallback", commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"), //默认为true 是否启用熔断
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "20"),  //默认熔断触发的最小个数20/10s 10s内请求失败数量达到20个，断路器开。
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000"), //熔断多少秒后去尝试请求 默认值：5000
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50") //失败率达到多少百分比后熔断  默认值：50 出错百分比阈值，当达到此阈值后，开始短路。默认50%
    })
    public CommonResult<Payment> hystrixTestGet(@PathVariable Integer id){
        log.info("Test访问");
        if(id <= 0){
            throw new RuntimeException("参数不能为负数");
        }
        CommonResult<Payment> result = new CommonResult<Payment>(1,"参数大于0 正确");
        return result;
    }

    public CommonResult<Payment> hystrixTestFallback(@PathVariable Integer id){
        return new CommonResult<Payment>(2,"参数小于等于0 错误" +"id为"+ id);
    }
}

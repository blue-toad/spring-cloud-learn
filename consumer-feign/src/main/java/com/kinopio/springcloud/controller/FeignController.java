package com.kinopio.springcloud.controller;

import com.kinopio.springcloud.entity.CommonResult;
import com.kinopio.springcloud.entity.Payment;
import com.kinopio.springcloud.service.PaymentFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class FeignController {
    @Autowired
    private PaymentFeignService feignService;

    @GetMapping("/consumer/payment/get/{id}")
    public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id){
        return feignService.getPaymentById(id);
    }
}

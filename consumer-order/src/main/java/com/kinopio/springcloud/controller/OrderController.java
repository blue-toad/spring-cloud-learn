package com.kinopio.springcloud.controller;

import com.kinopio.springcloud.entity.CommonResult;
import com.kinopio.springcloud.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@RestController
@Slf4j
public class OrderController {
    private static final String URL = "http://PAYMENT-SERVICE";

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/consumer/payment/create")
    public CommonResult<Payment> creatPayment(Payment payment) {
        log.info("发送请求的consumer{}", payment);
        CommonResult commonResult = restTemplate.postForObject(URL + "/payment/create", payment, CommonResult.class);
        log.info("template{}", commonResult);
        return commonResult;
    }

    @RequestMapping("/consumer/payment/get/{id}")
    public CommonResult<Payment> getPaymentByid(@PathVariable("id") Integer id) {
        log.info("发送的id={}", id);
        return restTemplate.getForObject(URL + "/payment/get/"+id , CommonResult.class);
    }

}

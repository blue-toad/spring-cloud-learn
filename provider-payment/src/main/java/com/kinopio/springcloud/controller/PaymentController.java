package com.kinopio.springcloud.controller;

import com.kinopio.springcloud.entity.CommonResult;
import com.kinopio.springcloud.entity.Payment;
import com.kinopio.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    //注意这里因为contentTypen不同 需要添加requestbody注解
    //当contentType为json时需要添加该注解
    @PostMapping("/payment/create")
    public CommonResult creatPayment(@RequestBody Payment payment){
        Integer result = paymentService.creatPayment(payment);
        log.info("插入信息{}结果{}", payment, result);
        if (result > 0){
            return new CommonResult(200, "插入数据库成功", result);
        }else {
            return new CommonResult(404, "插入失败");
        }
    }

    @GetMapping("/payment/get/{id}")
    public CommonResult creatPayment(@PathVariable("id") Integer id){
        Payment result = paymentService.getPaymentById(id);
        log.info("查询id{}结果{}", id, result);
        if (result != null){
            return new CommonResult(200, "查询数据库成功", result);
        }else {
            return new CommonResult(404, "查询失败");
        }
    }



}

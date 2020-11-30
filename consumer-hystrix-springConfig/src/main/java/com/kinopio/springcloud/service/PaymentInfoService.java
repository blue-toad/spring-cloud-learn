package com.kinopio.springcloud.service;

import com.kinopio.springcloud.entity.CommonResult;
import com.kinopio.springcloud.entity.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "PAYMENT-SERVICE")
public interface PaymentInfoService {
    @GetMapping("payment/get/{id}")
    CommonResult<Payment> getPaymentById(@PathVariable("id") Long id);

}
//fegin fallback 需要开启以下内容
//当前文件下
//@FeignClient(value = "PAYMENT-SERVICE", fallback = PaymentFallbackService.class)
//另一个接口实现类
//@Component
//public class PaymentFallbackService implements PaymentInfoService{
//    @Override
//    public CommonResult<Payment> getPaymentById(Long id) {
//        CommonResult<Payment> result = new CommonResult<Payment>(1,"系统错误或繁忙请稍后再试：+全局+fallback");
//        return result;
//    }
//}


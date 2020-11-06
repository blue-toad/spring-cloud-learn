package com.kinopio.springcloud.dao;

import com.kinopio.springcloud.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface PaymentDao {
    Integer creatPayment(Payment payment);
    Payment getPaymentById(Integer id);
}

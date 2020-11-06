package com.kinopio.springcloud.service;

import com.kinopio.springcloud.entity.Payment;

public interface PaymentService {
    Integer creatPayment(Payment payment);
    Payment getPaymentById(Integer id);
}

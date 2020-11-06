package com.kinopio.springcloud.service.impl;

import com.kinopio.springcloud.dao.PaymentDao;
import com.kinopio.springcloud.entity.Payment;
import com.kinopio.springcloud.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentDao paymentDao;

    @Override
    public Integer creatPayment(Payment payment) {
        return paymentDao.creatPayment(payment);
    }

    @Override
    public Payment getPaymentById(Integer id) {
        return paymentDao.getPaymentById(id);
    }
}

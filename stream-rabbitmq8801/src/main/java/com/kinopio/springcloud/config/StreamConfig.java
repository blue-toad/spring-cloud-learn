package com.kinopio.springcloud.config;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;


@Component
public interface StreamConfig {
    // 表示通道的名称
    String input = "myExchange";
    String output = "myExchange";

    @Output(output)
    MessageChannel output();

    @Input(input)
    SubscribableChannel input();
}

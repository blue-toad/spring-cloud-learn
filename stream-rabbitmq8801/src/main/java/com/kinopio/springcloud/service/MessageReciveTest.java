package com.kinopio.springcloud.service;

import com.kinopio.springcloud.config.StreamConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
public class MessageReciveTest {
    @StreamListener(StreamConfig.input)
    public void handleGreetings(Message<String> message) {
        log.info("收到信息: {}", message);
    }
}

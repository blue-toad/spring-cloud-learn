package com.kinopio.springcloud;

import com.kinopio.springcloud.config.StreamConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
public class MyStreamApplication8801Tests {
    @Autowired
    private StreamConfig stream;

    @Test
    public void send(){
        stream.output().send(MessageBuilder.withPayload("发送111消息").build());
        stream.output().send(MessageBuilder.withPayload("发送222消息").build());
        stream.output().send(MessageBuilder.withPayload("发送333消息").build());
        stream.output().send(MessageBuilder.withPayload("发送444消息").build());
    }
}

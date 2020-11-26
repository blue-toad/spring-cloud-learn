package com.kinopio.springcloud.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LogFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("测试自定义全局过滤器");
        String a = String.valueOf(exchange.getRequest().getPath());
        log.info(a);
        if(a == null) {
            //如果不存在
            log.info("不能通过过滤器");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete(); //请求结束
        }
        return chain.filter(exchange);  //继续向下执行
    }

    @Override
    public int getOrder() {
//        这个表示加载过滤器的顺序 返回值越小,执行优先级越高
        return 0;
    }
}

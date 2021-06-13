package com.geek.week11;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderAsyncHandler {

    public void createOrderAsync(String jsonStr) {
        log.info("异步创建订单入库，接收到的订单消息为：" + jsonStr);
    }
}

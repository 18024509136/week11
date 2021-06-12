package com.geek.week11;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;

public class StockDecrTest extends Week11ApplicationTests {

    @Autowired
    private StockService stockService;

    /**
     * 总共1000个库存。模拟20个线程，每个线程循环50次以上扣减库存操作，最后验证库存是否减到0就扣减失败
     */
    @Test
    public void test() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(20);
        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                for (int j = 0; j < 60; j++) {
                    int remainingStock = stockService.decrStock(1);
                    if (remainingStock >= 0) {
                        System.out.println("######扣减成功");
                    } else {
                        System.out.println("扣减失败");
                    }
                }
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();

        int finalStock = stockService.getStock();
        System.out.println("剩余库存为:" + finalStock);
    }
}

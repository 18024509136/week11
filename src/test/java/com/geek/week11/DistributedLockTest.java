package com.geek.week11;

import org.junit.jupiter.api.Test;
import org.redisson.RedissonRedLock;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author huangxiaodi
 * @since 2021-06-11 10:05
 */
public class DistributedLockTest extends Week11ApplicationTests {

    @Autowired
    private DistributedLockHandler distributedLockHandler;

    /**
     * 普通分布式锁测试，redis底层使用lettuce
     *
     * @throws Exception
     */
    @Test
    public void normalLockTest() throws Exception {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                String requestId = UUID.randomUUID().toString();
                try {
                    boolean isLock = distributedLockHandler.tryLock("myLock", requestId, 20000);
                    if (isLock) {
                        System.out.println("########" + Thread.currentThread().getName() + " 获取到锁，业务处理中...");
                        // 模拟业务处理时长
                        Thread.sleep(4000);
                    } else {
                        boolean isLockInSpin = distributedLockHandler.tryLockSpin("myLock", requestId, 20000);
                        if (isLockInSpin) {
                            System.out.println("########" + Thread.currentThread().getName() + " 获取到锁，业务处理中...");
                            // 模拟业务处理时长
                            Thread.sleep(4000);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    distributedLockHandler.unLock("myLock", requestId);
                }
            }).start();
        }

        Thread.sleep(30000);
    }

    /**
     * redisson redlock实现的分布式锁测试
     *
     * @throws Exception
     */
    @Test
    public void redissonLockTest() throws Exception {
        RedissonRedLock redissonRedLock = distributedLockHandler.getRedLock("myLock");

        // 模拟5个线程争夺分布式锁，来验证分布式锁是否生效
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    boolean isLock = redissonRedLock.tryLock(500, 5000, TimeUnit.MILLISECONDS);
                    if (isLock) {
                        System.out.println("########" + Thread.currentThread().getName() + " 获取到锁，业务处理中...");
                        // 模拟业务处理时长
                        Thread.sleep(4000);
                    } else {
                        boolean getLockInSpin = distributedLockHandler.tryRedLockInSpin(redissonRedLock, 500, 5000);
                        if (getLockInSpin) {
                            System.out.println("########" + Thread.currentThread().getName() + " 获取到锁，业务处理中...");
                            // 模拟业务处理时长
                            Thread.sleep(4000);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    redissonRedLock.unlock();
                }
            }).start();
        }

        Thread.sleep(30000);
    }
}

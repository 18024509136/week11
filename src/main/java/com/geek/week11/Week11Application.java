package com.geek.week11;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目开启前请按redissonConfig*.yml启动多个redis实例，否则会报错
 */
@SpringBootApplication
public class Week11Application {

    public static void main(String[] args) {
        SpringApplication.run(Week11Application.class, args);
    }

}

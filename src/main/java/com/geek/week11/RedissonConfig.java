package com.geek.week11;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * @author huangxiaodi
 * @since 2021-06-10 18:07
 */
@Configuration
public class RedissonConfig {

    @Bean("redissonClient1")
    public RedissonClient redissonClient1() throws Exception {
        Resource resource = new ClassPathResource("redissonConfig1.yml");
        InputStream is = resource.getInputStream();
        Config config = Config.fromYAML(is);
        return Redisson.create(config);
    }

    @Bean("redissonClient2")
    public RedissonClient redissonClient2() throws Exception {
        Resource resource = new ClassPathResource("redissonConfig2.yml");
        InputStream is = resource.getInputStream();
        Config config = Config.fromYAML(is);
        return Redisson.create(config);
    }

    @Bean("redissonClient3")
    public RedissonClient redissonClient3() throws Exception {
        Resource resource = new ClassPathResource("redissonConfig3.yml");
        InputStream is = resource.getInputStream();
        Config config = Config.fromYAML(is);
        return Redisson.create(config);
    }
}

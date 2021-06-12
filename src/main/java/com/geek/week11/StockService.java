package com.geek.week11;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DECR_LUA;

    private static final String STOCK_KEY = "myStock";

    static {
        /**
         * 当库存不够扣减的时候，返回-1
         * 当库存扣减成功的时候，返回扣减后库存
         */
        StringBuilder sb = new StringBuilder()
                .append("local stock = tonumber(redis.call('get', KEYS[1]));")
                .append("local decrNum = tonumber(ARGV[1]);")
                .append("local postStock = stock - decrNum;")
                .append("if(postStock >= 0) then")
                .append("     redis.call('decrby', KEYS[1], ARGV[1]);")
                .append("     return postStock;")
                .append("end;")
                .append("return -1;");
        DECR_LUA = sb.toString();
    }

    public int decrStock(int productNum) {
        Object[] keys1 = new Object[]{serialize(STOCK_KEY)};
        Object[] args1 = new Object[]{serialize(String.valueOf(productNum))};

        RedisCallback<Long> callback = (redisConnection -> {
            Object nativeConnection = redisConnection.getNativeConnection();
            return (Long) ((RedisAsyncCommands) nativeConnection).getStatefulConnection().sync().eval(DECR_LUA, ScriptOutputType.INTEGER, keys1, args1);
        });

        try {
            Long remainingStock = redisTemplate.execute(callback);
            return (int) (remainingStock.longValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getStock() {
        Object res = redisTemplate.opsForValue().get(STOCK_KEY);
        return (Integer) res;
    }

    private byte[] serialize(String key) {
        RedisSerializer<String> stringRedisSerializer =
                (RedisSerializer<String>) redisTemplate.getKeySerializer();
        //lettuce连接包下序列化键值，否则无法用默认的ByteArrayCodec解析
        return stringRedisSerializer.serialize(key);
    }

}

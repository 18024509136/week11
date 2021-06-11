package com.geek.week11;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.SetParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author huangxiaodi
 * @since 2021-06-11 10:01
 */

@Service
public class DistributedLockHandler {

    private static final int MAX_TRY_TIMES = 10;

    public static final String UNLOCK_LUA;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    @Qualifier("redissonClient1")
    private RedissonClient redissonClient1;

    @Autowired
    @Qualifier("redissonClient2")
    private RedissonClient redissonClient2;

    @Autowired
    @Qualifier("redissonClient3")
    private RedissonClient redissonClient3;

    /**
     * 尝试上锁
     *
     * @param key    锁的key
     * @param value  锁的内容，一般是上锁线程的唯一标识，解锁的时候也用到
     * @param expire 锁的租赁时长，单位毫秒
     * @return 上锁成功则返回true，失败则返回false
     */
    public boolean tryLock(String key, String value, long expire) {
        try {
            RedisCallback<Boolean> callback = (connection -> {
                try {
                    Object nativeConnection = connection.getNativeConnection();

                    String result = null;
                    if (nativeConnection instanceof Jedis) {
                        result = ((Jedis) nativeConnection).set(key, value, SetParams.setParams().nx().px(expire));
                    } else if (nativeConnection instanceof JedisCluster) {
                        result = ((JedisCluster) nativeConnection).set(key, value, SetParams.setParams().nx().px(expire));
                    } else if (nativeConnection instanceof RedisAsyncCommands) {
                        result = ((RedisAsyncCommands) nativeConnection).getStatefulConnection().sync().set(serialize(key),
                                serialize(value), SetArgs.Builder.nx().px(expire));
                    } else if (nativeConnection instanceof RedisAdvancedClusterAsyncCommands) {
                        result = ((RedisAdvancedClusterAsyncCommands) nativeConnection).getStatefulConnection().sync()
                                .set(serialize(key), serialize(value), SetArgs.Builder.nx().px(expire));
                    }
                    // 当上锁成功时返回OK，否则返回null
                    return StringUtils.isNotBlank(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            });
            return redisTemplate.execute(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取锁的租赁剩余时间
     *
     * @param key
     * @return 租赁剩余时间，单位毫秒
     */
    private long getLockExpire(String key) {
        // 当key不存在时getExpire返回-1
        return redisTemplate.getExpire(key) * 1000;
    }

    /**
     * 自旋获取锁
     *
     * @param key    锁的key
     * @param value  锁的内容，一般是上锁线程的唯一标识
     * @param expire 锁的租赁时长，单位毫秒
     * @return 上锁成功则返回true，失败则返回false
     */
    public boolean tryLockSpin(String key, String value, long expire) {
        int tryTime = 0;
        // 限定自旋最大尝试次数
        while (tryTime <= MAX_TRY_TIMES) {
            System.out.println(Thread.currentThread().getName() + " 获取锁失败，重新竞争锁");
            long lockExpire = getLockExpire(key);

            try {
                long retryDelay = lockExpire / 3;
                if (retryDelay > 0) {
                    Thread.sleep(retryDelay);
                }

                boolean tryLockResult = tryLock(key, value, expire);
                if (tryLockResult) {
                    return true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * 解锁
     *
     * @param key   锁的key
     * @param value 锁的内容，一般是上锁线程的唯一标识
     * @return 执行成功返回1，执行失败返回0
     */
    public boolean unLock(String key, String value) {
        try {
            // jedis和jedisCluster用的lua入参
            List<String> keys = new ArrayList<>();
            keys.add(key);
            List<String> args = new ArrayList<>();
            args.add(value);

            // lettuce用的lua入参
            Object[] keys1 = new Object[]{serialize(key)};
            Object[] args1 = new Object[]{serialize(value)};

            RedisCallback<Long> callback = (connection) -> {
                Object nativeConnection = connection.getNativeConnection();
                if (nativeConnection instanceof JedisCluster) {
                    return (Long) ((JedisCluster) nativeConnection).eval(UNLOCK_LUA, keys, args);
                }// 单机模式
                else if (nativeConnection instanceof Jedis) {
                    return (Long) ((Jedis) nativeConnection).eval(UNLOCK_LUA, keys, args);
                } else if (nativeConnection instanceof RedisAsyncCommands) {
                    return (Long) ((RedisAsyncCommands) nativeConnection).getStatefulConnection().sync().eval(UNLOCK_LUA, ScriptOutputType.INTEGER, keys1, args1);
                } else if (nativeConnection instanceof RedisAdvancedClusterAsyncCommands) {
                    return (Long) ((RedisAdvancedClusterAsyncCommands) nativeConnection).getStatefulConnection().sync().eval(UNLOCK_LUA, ScriptOutputType.INTEGER, keys1, args1);
                }
                return 0L;
            };
            Long result = redisTemplate.execute(callback);
            return result != null && result > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 获取redLock
     *
     * @param key 分布式锁的key
     * @return 返回redlock
     */
    public RedissonRedLock getRedLock(String key) {
        RLock lock1 = redissonClient1.getLock(key);
        RLock lock2 = redissonClient2.getLock(key);
        RLock lock3 = redissonClient3.getLock(key);

        return new RedissonRedLock(lock1, lock2, lock3);
    }

    /**
     * 自旋获取redlock
     *
     * @param redissonRedLock redlock对象
     * @param waitTime        获取锁等待时长，单位毫秒
     * @param leaseTime       锁的租赁时长，单位毫秒
     * @return 上锁成功则返回true，失败则返回false
     */
    public boolean tryRedLockInSpin(RedissonRedLock redissonRedLock, long waitTime, long leaseTime) {
        int tryTime = 0;
        // 限定自旋最大尝试次数
        while (tryTime <= MAX_TRY_TIMES) {
            System.out.println(Thread.currentThread().getName() + " 获取锁失败，重新竞争锁");
            // 以分布式锁的租赁时长的三分之一来重试获取锁
            long retryDelay = leaseTime / 3;

            try {
                Thread.sleep(retryDelay);
                boolean getLockResult = redissonRedLock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
                if (getLockResult) {
                    return true;
                }
                tryTime++;
            } catch (InterruptedException e) {
                e.printStackTrace();
                // 出现任何异常，均认定为获取锁失败，跳出方法
                return false;
            }
        }
        // 自旋此处超过最大尝试次数则认定为获取锁失败
        return false;
    }

    private byte[] serialize(String key) {
        RedisSerializer<String> stringRedisSerializer =
                (RedisSerializer<String>) redisTemplate.getKeySerializer();
        //lettuce连接包下序列化键值，否则无法用默认的ByteArrayCodec解析
        return stringRedisSerializer.serialize(key);
    }
}

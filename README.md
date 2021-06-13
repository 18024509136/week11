### 在 Java 中实现一个简单的分布式锁 ###
这里实现了两种分布式锁，一种是基于单机redis实例和LUA脚本的常规锁，一种是基于redisson red lock  
核心代码有:  
- 测试包下的com.geek.week11.DistributedLockTest，为功能测试类
- com.geek.week11.LettuceConfig，配置了基于redis单实例的redisTemplate
- com.geek.week11.RedissonConfig，基于多redis集群的redisson配置类
- com.geek.week11.DistributedLockHandler，实现常规锁和redlock的上锁和解锁

### 在 Java 中实现一个分布式计数器，模拟减库存 ###
实现了多线程同时扣减库存功能，当库存为0时，所有线程均扣减失败  
核心代码有：  
- 测试包下的com.geek.week11.StockDecrTest，为功能测试类
- com.geek.week11.StockService为库存服务类

### 基于 Redis 的 PubSub 实现订单异步处理 ###
实现了同步扣减库存，异步下单功能  
- com.geek.week11.OrderController为业务测试入口
- 消息发布端为com.geek.week11.OrderService，实现同步减库存和异步发送下单消息
- 消息消费端配置在com.geek.week11.LettuceConfig中，配置了RedisMessageListenerContainer、MessageListenerAdapter
- 消息消费类是com.geek.week11.OrderAsyncHandler，这里为了演示，就不入库了  
测试样例：  
请求url：http://localhost:8080/order/create  
请求方式：post  
请求消息(json)：  
{  
    "tradeId": 2222,  
    "userId": 22,  
    "totalNum": 2,  
    "totalAmount": 10000  
}  
成功响应消息： {"code": "0000", "msg":"成功", "data":订单号}

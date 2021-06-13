package com.geek.week11;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class OrderService {

    @Autowired
    @Qualifier("redissonClient1")
    private RedissonClient redissonClient;

    @Autowired
    private StockService stockService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private RBloomFilter<Long> bloomFilter;

    private Snowflake snowflake = IdUtil.createSnowflake(1L, 1L);

    /**
     * 初始化布隆过滤器
     */
    @PostConstruct
    private void getBloomFilter() {
        bloomFilter = redissonClient.getBloomFilter("tradeIdList");
        // 假设每天有3千万个交易，误差率为3%
        bloomFilter.tryInit(30000000L, 0.03);
    }

    /**
     * 减库存已经下单，同步减库存，异步下单
     * 库存扣减失败则不进行下单业务
     *
     * @param orderVo
     * @return
     */
    public Long createOrder(OrderVo orderVo) {
        try {
            // 布隆过滤器检查交易ID是否存在，不存在表示未处理过
            // 如果是表单重复提交，那么将会被拦截
            // 如果布隆过滤器出现误判，则页面生成新的交易ID再重试
            boolean alreadyProcess = bloomFilter.contains(orderVo.getTradeId());
            if (alreadyProcess) {
                throw new BusinessException(RespEnum.REPLICATED_TRADE_ID);
            }

            int remainingStock = stockService.decrStock(orderVo.getTotalNum());
            if (remainingStock < 0) {
                throw new BusinessException(RespEnum.NOT_ENOUGH_STOCK);
            }

            long orderNo = snowflake.nextId();
            OrderDto orderDto = new OrderDto(orderNo, orderVo.getUserId(), orderVo.getTotalNum(), orderVo.getTotalAmount());
            redisTemplate.convertAndSend(LettuceConfig.SUBSCRIBE_TOPIC, orderDto);
            bloomFilter.add(orderVo.getTradeId());
            return orderNo;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e1) {
            throw new BusinessException(RespEnum.SYSTEM_ERROR, e1);
        }
    }
}

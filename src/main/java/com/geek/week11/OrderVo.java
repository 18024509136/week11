package com.geek.week11;

import lombok.Data;

@Data
public class OrderVo {

    /**
     * 交易唯一标识，下订单页面预生成，格式为9位数字
     */
    private Long tradeId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 物品总数
     */
    private Integer totalNum;

    /**
     * 总金额
     */
    private Integer totalAmount;
}

package com.geek.week11;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderDto {

    /**
     * 订单号
     */
    private Long orderNo;

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

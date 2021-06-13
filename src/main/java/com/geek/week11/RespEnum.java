package com.geek.week11;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RespEnum {

    SUCCESS("0000", "成功"),
    REPLICATED_TRADE_ID("0001", "重复的交易"),
    NOT_ENOUGH_STOCK("0002", "库存不足"),
    SYSTEM_ERROR("9999", "系统异常");

    private String code;

    private String msg;

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

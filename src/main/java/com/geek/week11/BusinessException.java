package com.geek.week11;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException {

    private String code;

    public BusinessException(RespEnum respEnum) {
        super(respEnum.getMsg());
        this.code = respEnum.getCode();
    }

    public BusinessException(RespEnum respEnum, Throwable e) {
        super(respEnum.getMsg(), e);
        this.code = respEnum.getCode();
    }
}

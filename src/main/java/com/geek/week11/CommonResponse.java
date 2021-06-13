package com.geek.week11;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommonResponse {

    private String code;

    private String msg;

    private Object data;

    public CommonResponse(RespEnum respEnum) {
        this.code = respEnum.getCode();
        this.msg = respEnum.getMsg();
    }

    public CommonResponse(Object data) {
        this(RespEnum.SUCCESS);
        this.data = data;
    }

    public CommonResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private static final CommonResponse SUCCESS = new CommonResponse(RespEnum.SUCCESS);

    private static final CommonResponse SYSTEM_ERROR = new CommonResponse(RespEnum.SYSTEM_ERROR);
}
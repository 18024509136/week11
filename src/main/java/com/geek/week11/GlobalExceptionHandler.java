package com.geek.week11;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public Object defaultErrorHandler(HttpServletRequest req, Exception e) {
        log.error("系统异常，" + e.getMessage(), e);
        if (e instanceof BusinessException) {
            BusinessException e1 = (BusinessException) e;
            return new CommonResponse(e1.getCode(), e1.getMessage());
        }
        return new CommonResponse(RespEnum.SYSTEM_ERROR.getCode(), e.getMessage());
    }
}

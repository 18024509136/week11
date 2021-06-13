package com.geek.week11;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public CommonResponse createOrder(@RequestBody OrderVo orderVo) {
        Long orderNo = orderService.createOrder(orderVo);
        return new CommonResponse(orderNo);
    }
}

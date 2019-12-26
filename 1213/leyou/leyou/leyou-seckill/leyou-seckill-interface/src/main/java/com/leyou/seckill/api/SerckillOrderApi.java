package com.leyou.seckill.api;

import com.leyou.seckill.pojo.SeckillOrder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(value = "/seckill/order")
public interface SerckillOrderApi {
    @GetMapping(value = "/ordername")
    public SeckillOrder getOrderName(@RequestParam(value = "username",required = false) String username);
    @GetMapping(value = "/updateOrder")
    public void updateOrder(@RequestParam(value = "username",required = false) String username,
                            @RequestParam(value = "id)",required = false) String id);
}

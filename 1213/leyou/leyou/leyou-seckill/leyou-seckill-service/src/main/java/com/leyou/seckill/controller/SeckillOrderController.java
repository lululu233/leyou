package com.leyou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.leyou.common.pojo.UserInfo;
import com.leyou.seckill.interceptor.LoginInterceptor;
import com.leyou.seckill.pojo.Result;
import com.leyou.seckill.pojo.SeckillOrder;
import com.leyou.seckill.service.SeckillOrderService;
import com.leyou.seckill.service.impl.SeckillOrderServiceImpl;
import com.leyou.utils.PayHelper;
import com.leyou.utils.PayState;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/***
 *
 * @Author:shenkunlin
 * @Description:itheima
 * @date: 2018/9/28 17:27
 *
 ****/
@RestController
@RequestMapping(value = "/seckill/order")
public class SeckillOrderController {

    @Autowired
    private PayHelper payHelper;

    @Autowired
    private SeckillOrderService seckillOrderService;

    /***
     * 增加订单
     * @param id
     * @return
     */
    @GetMapping(value = "/add/{id}")
    public Long add(@PathVariable("id") Long id){
        try {
            UserInfo user = LoginInterceptor.getLoginUser();
            //下订单
            Long orderId = seckillOrderService.add(user.getUsername(), id);

            return  orderId;
        } catch (Exception e) {
            return  null;
        }
    }

    @GetMapping(value = "/ordername")
    public SeckillOrder getOrderName(@RequestParam(value = "username",required = false) String username){
        return this.seckillOrderService.getOrderByUserName(username);
    }

    @GetMapping(value = "/updateOrder")
    public void updateOrder(@RequestParam(value = "username",required = false) String username,
                            @RequestParam(value = "id)",required = false) String id){
        this.seckillOrderService.updatePayStatus(username,id);
    }

    /**
     * 生成付款链接
     *
     * @param orderId
     * @return
     */
    @GetMapping("url/{id}")
    public ResponseEntity<String> generateUrl(@PathVariable("id") Long orderId) {


        // 生成付款链接
        String url = this.payHelper.createPayUrl(orderId);
        if (StringUtils.isBlank(url)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(url);
    }

    /**
     * 查询付款状态
     *
     * @param orderId
     * @return 0, 状态查询失败 1,支付成功 2,支付失败
     */
    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryPayState(@PathVariable("id") String orderId) {
        PayState payState = this.payHelper.queryOrder(orderId);
        return ResponseEntity.ok(payState.getValue());
    }



}

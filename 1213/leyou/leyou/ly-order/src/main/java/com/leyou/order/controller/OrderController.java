package com.leyou.order.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.common.pojo.UserInfo;
import com.leyou.order.client.SeckillOrderClient;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.pojo.LogisticsB;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import com.leyou.seckill.pojo.SeckillOrder;
import com.leyou.seckill.service.SeckillOrderService;
import com.leyou.utils.PayHelper;
import com.leyou.utils.PayState;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-05-04 10:12
 **/

@RestController
@RequestMapping("order")
@Api("订单服务接口")
public class OrderController {

    @Autowired
    private SeckillOrderClient seckillOrderClient;

    private static final Logger logger = LoggerFactory.getLogger(PayHelper.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private PayHelper payHelper;

    /**
     * 创建订单
     *
     * @param order 订单对象
     * @return 订单编号
     */
    @PostMapping
    @ApiOperation(value = "创建订单接口，返回订单编号", notes = "创建订单")
    @ApiImplicitParam(name = "order", required = true, value = "订单的json对象,包含订单条目和物流信息")
    public ResponseEntity<Long> createOrder(@RequestBody @Valid Order order) {
        Long id = this.orderService.createOrder(order);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

    /**
     * 根据订单编号查询订单
     *
     * @param id
     * @return
     */
    @GetMapping("{id}")
    @ApiOperation(value = "根据订单编号查询订单，返回订单对象", notes = "查询订单")
    @ApiImplicitParam(name = "id", required = true, value = "订单的编号")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id) {
        Order order = this.orderService.queryById(id);
        if (order == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(order);
    }

    /**
     * 后台
     * 查询所有订单
     *
     * @param
     * @return
     */
    @GetMapping("queryOrders")
    @ApiOperation(value = "可根据status 查询所有用户订单，返回订单对象集合", notes = "后台查询所有订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "当前页", defaultValue = "1", type = "Integer"),
            @ApiImplicitParam(name = "rows", value = "每页大小", defaultValue = "5", type = "Integer"),
            @ApiImplicitParam(name = "status", value = "订单状态：1未付款，2已付款未发货，3已发货未确认，4已确认未评价，5交易关闭，6交易成功，已评价", type = "Integer"),
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "订单的分页结果"),
            @ApiResponse(code = 404, message = "没有查询到结果"),
            @ApiResponse(code = 500, message = "查询失败"),
    })
    public ResponseEntity<PageResult<Order>> queryOrders(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<Order> result = this.orderService.queryOrders(page, rows, status);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }




    /**
     * 分页查询当前用户订单
     *
     * @param status 订单状态
     * @return 分页订单数据
     */
    @GetMapping("list")
    @ApiOperation(value = "分页查询当前用户订单，并且可以根据订单状态过滤", notes = "分页查询当前用户订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "当前页", defaultValue = "1", type = "Integer"),
            @ApiImplicitParam(name = "rows", value = "每页大小", defaultValue = "5", type = "Integer"),
            @ApiImplicitParam(name = "status", value = "订单状态：1未付款，2已付款未发货，3已发货未确认，4已确认未评价，5交易关闭，6交易成功，已评价", type = "Integer"),
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "订单的分页结果"),
            @ApiResponse(code = 404, message = "没有查询到结果"),
            @ApiResponse(code = 500, message = "查询失败"),
    })
    public ResponseEntity<PageResult<Order>> queryUserOrderList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<Order> result = this.orderService.queryUserOrderList(page, rows, status);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 更新订单状态
     *
     * @param id
     * @param status
     * @return
     */
    @PutMapping("/updateStatus/{id}/{status}")
    @ApiOperation(value = "更新订单状态", notes = "更新订单状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "订单编号", type = "Long"),
            @ApiImplicitParam(name = "status", value = "订单状态：1未付款，2已付款未发货，3已发货未确认，4已确认未评价，5交易关闭，6交易成功，已评价", type = "Integer"),
    })

    @ApiResponses({
            @ApiResponse(code = 204, message = "true：修改状态成功；false：修改状态失败"),
            @ApiResponse(code = 400, message = "请求参数有误"),
            @ApiResponse(code = 500, message = "查询失败")
    })
    public ResponseEntity<Boolean> updateStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status) {
        Boolean boo = this.orderService.updateStatus(id, status);
        if (boo == null) {
            // 返回400
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // 返回204
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * 生成付款链接
     *
     * @param orderId
     * @return
     */
    @GetMapping("url/{id}")
    @ApiOperation(value = "生成微信扫码支付付款链接", notes = "生成付款链接")
    @ApiImplicitParam(name = "id", value = "订单编号", type = "Long")
    @ApiResponses({
            @ApiResponse(code = 200, message = "根据订单编号生成的微信支付地址"),
            @ApiResponse(code = 404, message = "生成链接失败"),
            @ApiResponse(code = 500, message = "服务器异常"),
    })
    public ResponseEntity<String> generateUrl(@PathVariable("id") Long orderId) {
        //获取redis订单信息
        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser();
        try {
            SeckillOrder seckillOrder = seckillOrderClient.getOrderName(user.getUsername());
            //SeckillOrder seckillOrder = (SeckillOrder) redisTemplateOrder.boundHashOps("SeckillOrder").get(user.getUsername());
            if (seckillOrder != null){
                orderId = seckillOrder.getId();
            }
        }catch (Exception e){
            logger.error("查询redis order异常,订单编号：{}", orderId, e);
        }

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
    @ApiOperation(value = "查询扫码支付付款状态", notes = "查询付款状态")
    @ApiImplicitParam(name = "id", value = "订单编号", type = "Long")
    @ApiResponses({
            @ApiResponse(code = 200, message = "0, 未查询到支付信息 1,支付成功 2,支付失败"),
            @ApiResponse(code = 500, message = "服务器异常"),
    })
    public ResponseEntity<Integer> queryPayState(@PathVariable("id") Long orderId) {
        PayState payState = this.payHelper.queryOrder(orderId);
        return ResponseEntity.ok(payState.getValue());
    }

    /*-------------------------------------------------------------------------*/
    /**
     * 删除订单
     *
     * @param orderId
     * @return
     */
    @GetMapping("deleteOrder/{id}")
    @ApiOperation(value = "删除订单", notes = "删除订单")
    @ApiImplicitParam(name = "id", value = "订单编号", type = "Long")
    @ApiResponses({
            @ApiResponse(code = 200, message = "删除成功"),
            @ApiResponse(code = 500, message = "服务器异常"),
    })
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long orderId) {
        Integer result = orderService.deleteOrder(orderId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("selectLogistics")
    public ResponseEntity<List<LogisticsB>> selectLogistics() {
        List<LogisticsB> results = orderService.selectLogistics();
        if (results == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(results);

    }

    /**
     * 创建物流
     *
     * @param
     * @return
     */
    @GetMapping("createLogistics")
    public ResponseEntity<Long> createLogistics(@RequestParam(value = "orderId") Long orderId,
                                                @RequestParam(value = "select") Integer select,
                                                @RequestParam(value = "logisticsId") Long logisticsId) {

       Long result = this.orderService.createLogistics(orderId,select,logisticsId);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);


    }



}

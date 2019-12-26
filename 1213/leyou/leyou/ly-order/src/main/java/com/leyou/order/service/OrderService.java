package com.leyou.order.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.leyou.common.pojo.PageResult;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.interceptor.LoginInterceptor;
import com.leyou.order.mapper.*;
import com.leyou.order.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-05-04 10:11
 **/
@Service
public class OrderService {

    @Autowired
    private LogisticsMapper logisticsMapper;

    @Autowired
    private LogisticsBMapper logisticsBMapper;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper detailMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Transactional
    public Long createOrder(Order order) {

        order.getOrderDetails().forEach(orderDetail -> {
            //删除sku的stock
            SpuBo spuBo = new SpuBo();
            spuBo.setId(orderDetail.getSkuId());
            spuBo.setStock(orderDetail.getNum());
            this.goodsClient.updateStockGoods(spuBo);
        });


        // 生成orderId
        long orderId = idWorker.nextId();
        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser();
        // 初始化数据
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setUserId(user.getId());
        // 保存数据
        this.orderMapper.insertSelective(order);

        // 保存订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(1);// 初始状态为未付款

        this.statusMapper.insertSelective(orderStatus);

        // 订单详情中添加orderId cid1 cid2 cid3 计算 money
       /* cartList.forEach(cart -> {
            //根据sku 查 spuId
            Sku sku = this.goodsClient.querySkuBySkuId(cart.getSkuId());
            //根据spuid 查 cid3
            Spu spu = this.goodsClient.querySpuById(sku.getSpuId());
            //设置到 cart
            cart.setCategoryId(spu.getCid3());

            //计算cart一项 商品总额 num*price
            cart.setSumPrice(cart.getPrice() * cart.getNum());
        });*/
        List<OrderDetail> orderDetails = order.getOrderDetails();
        orderDetails.forEach(od -> {
            Sku sku = this.goodsClient.querySkuBySkuId(od.getSkuId());
            Spu spu = this.goodsClient.querySpuById(sku.getSpuId());
            od.setCategoryId1(spu.getCid1().intValue());
            od.setCategoryId2(spu.getCid2().intValue());
            od.setCategoryId3(spu.getCid3().intValue());
            od.setMoney(od.getNum() * od.getPrice().intValue());
            od.setOrderId(orderId);

        });
        // 保存订单详情,使用批量插入功能
        this.detailMapper.insertList(order.getOrderDetails());

        logger.debug("生成订单，订单编号：{}，用户id：{}", orderId, user.getId());

        return orderId;
    }

    public Order queryById(Long id) {
        // 查询订单
        Order order = this.orderMapper.selectByPrimaryKey(id);

        // 查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = this.detailMapper.select(detail);
        order.setOrderDetails(details);

        // 查询订单状态
        OrderStatus status = this.statusMapper.selectByPrimaryKey(order.getOrderId());
        order.setStatus(status.getStatus());
        return order;
    }

    public PageResult<Order> queryUserOrderList(Integer page, Integer rows, Integer status) {
        try {
            // 分页
            PageHelper.startPage(page, rows);
            // 获取登录用户
            UserInfo user = LoginInterceptor.getLoginUser();
            // 创建查询条件
            Page<Order> pageInfo = (Page<Order>) this.orderMapper.queryOrderList(user.getId(), status);

            return new PageResult<>(pageInfo.getTotal(), pageInfo);
        } catch (Exception e) {
            logger.error("查询订单出错", e);
            return null;
        }
    }

    @Transactional
    public Boolean updateStatus(Long id, Integer status) {
        OrderStatus record = new OrderStatus();
        record.setOrderId(id);
        record.setStatus(status);
        // 根据状态判断要修改的时间
        switch (status) {
            case 2:
                record.setPaymentTime(new Date());// 付款
                break;
            case 3:
                record.setConsignTime(new Date());// 发货
                break;
            case 4:
                record.setEndTime(new Date());// 确认收获，订单结束
                break;
            case 5:
                record.setCloseTime(new Date());// 交易失败，订单关闭
                break;
            case 6:
                record.setCommentTime(new Date());// 评价时间
                break;
            default:
                return null;
        }
        int count = this.statusMapper.updateByPrimaryKeySelective(record);
        return count == 1;
    }

    public PageResult<Order> queryOrders(Integer page, Integer rows, Integer status) {
        try {
            // 分页
            PageHelper.startPage(page, rows);
            // 创建查询条件
            Page<Order> pageInfo = (Page<Order>) this.orderMapper.queryOrders(status);
            //List<Order> orders = this.orderMapper.selectAll();


            return new PageResult<>(pageInfo.getTotal(), pageInfo);
        } catch (Exception e) {
            logger.error("查询订单出错", e);
            return null;
        }
    }

    @Transactional
    public Integer deleteOrder(Long orderId) {
        orderMapper.deleteByPrimaryKey(orderId);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        detailMapper.delete(orderDetail);

        statusMapper.deleteByPrimaryKey(orderId);
        return 1;
    }

    public PageResult<Order> allOrders(Integer page, Integer rows) {
        try {
            // 分页
            PageHelper.startPage(page, rows);
            // 创建查询条件
            Order order = new Order();

            Page<Order> pageInfo = (Page<Order>) this.orderMapper.selectByExample(null);


            return new PageResult<>(pageInfo.getTotal(), pageInfo);
        } catch (Exception e) {
            logger.error("查询订单出错", e);
            return null;
        }
    }


    public List<LogisticsB> selectLogistics() {

        List<LogisticsB> results = this.logisticsBMapper.selectAll();
        return results;

    }

    @Transactional
    public Long createLogistics(Long orderId, Integer select, Long logisticsId) {
        //查看订单
        Order order = this.orderMapper.selectByPrimaryKey(orderId);
        //查看物流商地址
        LogisticsB logistics_B = this.logisticsBMapper.selectByPrimaryKey(select);

        Logistics logistics = new Logistics();
        logistics.setOrderId(orderId);
        logistics.setAddressId(null);
        logistics.setId(logisticsId);
        logistics.setReceiverAddress(order.getReceiverAddress());
        logistics.setShippingAddress(logistics_B.getShippingAddress());
        logistics.setCreateTime(new Date());
        int i = this.logisticsMapper.insertSelective(logistics);
        if (i != 0){
            //将订单标记发货
            OrderStatus record = new OrderStatus();
            record.setOrderId(order.getOrderId());
            record.setStatus(3);
            record.setConsignTime(new Date());// 发货
            int count = this.statusMapper.updateByPrimaryKeySelective(record);
            if (count == 0){
                return null;
            }
            return logistics.getId();
        }
        return null;
    }


}

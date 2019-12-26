package com.leyou.order.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.order.mapper.*;
import com.leyou.order.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class LogisticsService {

    @Autowired
    private LogisticsMapper logisticsMapper;

    @Autowired
    private LogisticsBMapper logisticsBMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper statusMapper;

    private static final Logger logger = LoggerFactory.getLogger(LogisticsService.class);


    public List<LogisticsB> selectLogisticsB() {

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

    public PageResult<Logistics> queryLogistics(String key, Integer page, Integer rows) {
        //初始化example对象
        Example example = new Example(Logistics.class);
        Example.Criteria criteria = example.createCriteria();

        //key  根据name orEqualTo 首字母 模糊查询
        if(StringUtils.isNotBlank(key)){//StringUtils apache 的包
            criteria.andLike("name","%" + key + "%");
        }

        //添加分页条件 page rows
        PageHelper.startPage(page,rows);



        List<Logistics> brands = this.logisticsMapper.selectByExample(example);
        //包装pageInfo 对象
        PageInfo<Logistics> pageInfo = new PageInfo<>(brands);
        //包装 分页结果对象返回
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());

    }

    public PageResult<LogisticsB> queryLogisticsB(String key, Integer page, Integer rows) {
        //初始化example对象
        Example example = new Example(LogisticsB.class);
        Example.Criteria criteria = example.createCriteria();

        //key  根据name orEqualTo 首字母 模糊查询
        if(StringUtils.isNotBlank(key)){//StringUtils apache 的包
            criteria.andLike("name","%" + key + "%");
        }

        //添加分页条件 page rows
        PageHelper.startPage(page,rows);



        List<LogisticsB> brands = this.logisticsBMapper.selectByExample(example);
        //包装pageInfo 对象
        PageInfo<LogisticsB> pageInfo = new PageInfo<>(brands);
        //包装 分页结果对象返回
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());



    }
}

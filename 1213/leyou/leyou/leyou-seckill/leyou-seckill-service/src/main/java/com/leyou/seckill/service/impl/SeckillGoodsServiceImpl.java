package com.leyou.seckill.service.impl;

import com.leyou.seckill.pojo.SeckillGoods;
import com.leyou.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService{
    @Autowired
    private RedisTemplate redisTemplate;


    /***
     * 根据商品ID查询秒杀商品信息
     * @param id
     * @return
     */
    @Override
    public SeckillGoods getOne(Long id) {
        return (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods").get(id);
    }

    /***
     * 商品秒杀列表
     * @return
     */
    @Override
    public List<SeckillGoods> list() {
        return redisTemplate.boundHashOps("SeckillGoods").values();
    }

}

package com.leyou.seckill.service;

import com.leyou.seckill.pojo.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {
    public List<SeckillGoods> list();
    public SeckillGoods getOne(Long id);
}

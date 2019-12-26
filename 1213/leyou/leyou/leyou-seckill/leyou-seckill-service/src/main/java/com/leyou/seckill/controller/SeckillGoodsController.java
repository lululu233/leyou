package com.leyou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.leyou.seckill.pojo.SeckillGoods;
import com.leyou.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/***
 *
 * @Author:shenkunlin
 * @Description:itheima
 * @date: 2018/9/28 16:08
 *
 ****/
@RestController
@RequestMapping(value = "/seckill/goods")
public class SeckillGoodsController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    /***
     * 根据ID查询商品详情
     * @param id
     * @return
     */
    @GetMapping(value = "/one/{id}")
    public SeckillGoods getOne(@PathVariable("id")Long id){
        return seckillGoodsService.getOne(id);
    }



    /****
     * 秒杀商品列表
     * @return
     */
    @GetMapping(value = "/list")
    public List<SeckillGoods> list(){
        return  seckillGoodsService.list();
    }

}

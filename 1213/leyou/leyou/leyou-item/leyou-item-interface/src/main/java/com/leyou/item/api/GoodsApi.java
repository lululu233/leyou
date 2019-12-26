package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public interface GoodsApi {


    /**
     * 根据条件 分页查询 spu
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")//?key=&saleable=true&page=1&rows=5 saleable 上架下架 不赋予默认值
    public PageResult<SpuBo> querySpuByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows
    );


    /**
     * 根据spuid 查询 spudetail
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    public SpuDetail querySpuDetailBySpuId(@PathVariable("spuId")Long spuId);

    /**
     * 根据 spuid 查询 sku集合
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    public List<Sku> querySkusBySpuId(@RequestParam("id")Long spuId);

    /**
     * 根据id 查 spu
     * @param
     * @return
     */
    @GetMapping("{spuId}")
    public Spu querySpuById(@PathVariable("spuId")Long spuId);


    /**
     * 根据skuId 查询 sku
     * @param skuId
     * @return
     */
    @GetMapping("sku/{skuId}")
    public Sku querySkuBySkuId(@PathVariable("skuId")Long skuId);


    /**更新商品数据
     * @param spuBo
     * @return
     */
    @PutMapping("goods")
    public void updateGoods(@RequestBody SpuBo spuBo);

    /**
     * 更新商品库存
     * @param spuBo
     * @return
     */
    @PutMapping("updateStockGoods")
    public Void updateStockGoods(@RequestBody SpuBo spuBo);
}

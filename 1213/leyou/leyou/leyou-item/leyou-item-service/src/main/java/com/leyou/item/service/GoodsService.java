package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 根据条件 分页查询 spu
     *
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //添加查询条件
        if (StringUtils.isNotBlank(key)) {//StringUtils.isNotBlank(key) 判断key 是否为空
            criteria.andLike("title", "%" + key + "%");
        }

        //添加上下架过滤条件
        if (saleable != null) {//criteria.andEqualTo 求 上面 与添加条件的交集
            criteria.andEqualTo("saleable", saleable);
        }

        //添加分页
        PageHelper.startPage(page, rows);

        //执行查询 得到 spu
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);//返回总条数

        //spu 集合 转化成 spubo
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();

            //复制属性值 spu 复制到 spuBo
            BeanUtils.copyProperties(spu, spuBo);

            //查询品牌名
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());

            //查询类名称
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            //StringUtils.join(names,"-") 用 - 分割 转字符串
            spuBo.setCname(StringUtils.join(names, "-"));
            return spuBo;//必须有返回值
        }).collect(Collectors.toList());
        //返回 pageResult<spuBo>
        return new PageResult<>(pageInfo.getTotal(), spuBos);

    }

    /**
     * 新增商品 需要添加事务防止失败是时的垃圾数据
     *
     * @param spuBo
     * @return
     */
    @Transactional //添加事务
    public void saveGoods(SpuBo spuBo) {
        //1 先新增spu
        spuBo.setId(null);//防止恶意注入
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);

        //2 再新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

        //3 遍历新增sku stock
        saveSkuAndStock(spuBo);

        //发送消息到队列
        sendMsg("insert", spuBo.getId());


    }

    /**
     * 消息队列
     *
     * @param type
     * @param id
     */
    private void sendMsg(String type, Long id) {
        //发送到队列
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    private void saveSkuAndStock(SpuBo spuBo) {
        spuBo.getSkus().forEach(sku -> {
            //sku
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);
            //stock 库存 相当于 与sku 为同一张表
            //4 新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    /**
     * 创建订单减少商品库存
     *
     * @param
     */
    private void deletStock(Sku sku, Integer num) {


        //库存-订单中的sku数量
        Integer updateStock = sku.getStock() - num;
        //更新数据表
        Stock stock = new Stock();
        stock.setSkuId(sku.getId());
        stock.setStock(updateStock);
        this.stockMapper.updateByPrimaryKey(stock);



    }

    /**
     * 根据spuid 查询 spudetail
     *
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailMapper.selectByPrimaryKey(spuId);//spuid 为对应查询表的主键使用此方法

    }

    /**
     * 根据 spuid 查询 sku集合
     *
     * @param spuId
     * @return
     */
    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(record);
        skus.forEach(sku -> {
            //查找库存
            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });
        return skus;

    }


    /**
     * 更新商品 先删除 字表 在新增 主表
     *
     * @param spuBo
     */
    @Transactional
    public void updateGoods(SpuBo spuBo) {

        //根据spuid 查询要删除的sku
        Sku record = new Sku();
        record.setSpuId(spuBo.getId());
        List<Sku> skus = this.skuMapper.select(record);

        skus.forEach(sku -> {
            //1 操作sku  删除 stock
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        });


        //2 删除sku  在循环内？
        Sku sku = new Sku();
        sku.setSpuId(spuBo.getId());
        this.skuMapper.delete(sku);

        //3 新增sku 4 新增stock

        this.saveSkuAndStock(spuBo);

        //5 更新spu 和 spuDetail
        spuBo.setCreateTime(null);//需要默认注入数据 防止 外部恶意注入
        spuBo.setLastUpdateTime(new Date());
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);


        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        //发送消息到队列
        sendMsg("update", spuBo.getId());
    }

    public Spu querySpuById(Long spuId) {
        return this.spuMapper.selectByPrimaryKey(spuId);
    }

    public Sku querySkuBySkuId(Long skuId) {
        return this.skuMapper.selectByPrimaryKey(skuId);
    }

    /**
     * 更新库存
     *
     * @param spuBo
     */
    @Transactional
    public void updateStockGoods(SpuBo spuBo) {

        //根据spuid 查询要删除的sku
        Sku record = new Sku();
        //获取订单中的数量
        Integer num = spuBo.getStock();
        record = this.skuMapper.selectByPrimaryKey(spuBo.getId());
        Stock stock = this.stockMapper.selectByPrimaryKey(spuBo.getId());
        record.setStock(stock.getStock());

        //1 操作sku  删除 stock
        this.deletStock(record, num);


        //发送消息到队列
        sendMsg("update", spuBo.getId());

    }

    @Transactional
    public int[] deleteGoodBySpuId(Long spuId) {
        int deleteSums[] = {0,0,0};
        Sku record = new Sku();
        record.setSpuId(spuId);

        List<Sku> skus = this.skuMapper.select(record);

        skus.forEach(sku -> {
            //删除stock
            int j = this.stockMapper.deleteByPrimaryKey(sku.getId());
            //删除sku
            int i = this.skuMapper.deleteByPrimaryKey(sku.getId());
            deleteSums[0] += i;


        });
        //删除spu_detail
        deleteSums[1] = this.spuDetailMapper.deleteByPrimaryKey(spuId);
        //删除spu
        deleteSums[2] = this.spuMapper.deleteByPrimaryKey(spuId);

        return deleteSums;
    }
}

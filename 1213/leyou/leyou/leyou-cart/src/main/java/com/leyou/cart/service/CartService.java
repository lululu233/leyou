package com.leyou.cart.service;

import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.client.PreferentialClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

   /* @Autowired
    PreferentialServiceImpl preferentialService;*/

   @Autowired
   private PreferentialClient preferentialClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    private static final String KEY_PREFIX = "user:cart:";

    public void addCart(Cart cart) {
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //查询购物车记录 String --> 传递的 string key
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        String key = cart.getSkuId().toString();
        Integer num = cart.getNum();
        //判断当前购物车商品是否在购物车中
        if(hashOperations.hasKey(key)){
            //更新 数量
            String cartJson = hashOperations.get(key).toString();
            //序列化为 cart对象
            cart = JsonUtils.parse(cartJson, Cart.class);
            cart.setNum(cart.getNum() + num);

        }else {
            //不在 新增购物车
            //查询sku
            Sku sku = this.goodsClient.querySkuBySkuId(cart.getSkuId());

            cart.setUserId(userInfo.getId());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            cart.setPrice(sku.getPrice());

        }

        //无论商品在不在 都加到 redis中
        hashOperations.put(key, JsonUtils.serialize(cart));

    }

    public List<Cart> queryCarts() {

        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //判断用户key 是否存在
        if (!this.redisTemplate.hasKey(KEY_PREFIX + userInfo.getId())){
            return null;
        }

        //获取用户购物车记录
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        //获取map 中的value 默认获取的为json格式
        List<Object> cartsJson = hashOperations.values();

        //购物车集合为空
        if (CollectionUtils.isEmpty(cartsJson)){
            return null;
        }

        //反序列化
        return cartsJson.stream().map(cartJson -> JsonUtils.parse(cartJson.toString(), Cart.class)).collect(Collectors.toList());
    }

    public void updateNum(Cart cart) {
        //获取用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //判断用户key 是否存在
        if (!this.redisTemplate.hasKey(KEY_PREFIX + userInfo.getId())){
            return ;
        }

        Integer num = cart.getNum();

        //获取用户购物车记录
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());

        String cartJson = hashOperations.get(cart.getSkuId().toString()).toString();

        cart = JsonUtils.parse(cartJson, Cart.class);

        cart.setNum(num);

        hashOperations.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));
    }

    public void deleteCart(String skuId) {
        // 获取登录用户
        UserInfo user = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX + user.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        hashOps.delete(skuId);
    }


    public int preferential(List<Cart> cartList) {
        cartList.forEach(cart -> {
            //根据sku 查 spuId
            Sku sku = this.goodsClient.querySkuBySkuId(cart.getSkuId());
            //根据spuid 查 cid3
            Spu spu = this.goodsClient.querySpuById(sku.getSpuId());
            //设置到 cart
            cart.setCategoryId(spu.getCid3());

            //计算cart一项 商品总额 num*price
            cart.setSumPrice(cart.getPrice() * cart.getNum());
        });


        //获取选中的购物车
        //按分类聚合统计每个分类的金额
        Map<Long, LongSummaryStatistics> collect = cartList.stream().collect(Collectors.groupingBy(Cart::getCategoryId, Collectors.summarizingLong(Cart::getSumPrice)));
        int allPreMoney=0;//优惠金额
        //循环分类
        for( Long categoryId :collect.keySet() ){
            int  money = (int)collect.get(categoryId).getSum();//品类消费金额合计
            //System.out.println("分类："+categoryId+"  金额："+money);
            allPreMoney += preferentialClient.findPreMoneyByCategoryId(categoryId, new Long(money));//根据分类ID和消费金额查询优惠金额
        }
        return allPreMoney;

    }
}

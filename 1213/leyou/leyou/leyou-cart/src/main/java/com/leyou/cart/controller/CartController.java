package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        this.cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询购物车
     * @return
     */
    @GetMapping
    public ResponseEntity<List<Cart>> queryCarts(){

        List<Cart> carts = this.cartService.queryCarts();
        if (CollectionUtils.isEmpty(carts)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carts);
    }

    /**
     * 更新数量
     * @param cart
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestBody Cart cart){
        this.cartService.updateNum(cart);
        return ResponseEntity.noContent().build();

    }

    /**
     * 删除购物车
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") String skuId) {
        this.cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }

    /*** 计算当前选中的购物车的优惠金额 * @param * @return */
    @PostMapping("cartSum")
    public ResponseEntity<Map> preferential(@RequestBody List<Cart> cartList){
        //String username = SecurityContextHolder.getContext().getAuthentication().getName();


        int preferential = cartService.preferential(cartList);
        Map map=new HashMap();
        map.put("preferential",preferential);
        return  ResponseEntity.ok(map);
    }

}

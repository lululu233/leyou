package com.leyou.order.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.order.pojo.LogisticsB;
import com.leyou.order.pojo.Preferential;
import com.leyou.order.service.PreferentialService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("preferential")
@Api("优惠券管理")
public class PreferentialController {

    @Autowired
    private PreferentialService preferentialService;

    @PostMapping("selectPreferential")
    public ResponseEntity<Integer> findPreMoneyByCategoryId(@RequestParam(value = "categoryId") Long categoryId,@RequestParam(value = "money") Long money) {

        Integer result = preferentialService.findPreMoneyByCategoryId(categoryId,money.intValue());
        if (result == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);

    }


    @GetMapping("queryPreferentials")/*required = false 不知道默认值 设置这个*/
    public ResponseEntity<PageResult<Preferential>> queryBrandsByPage(
            @RequestParam(value = "key", required = false)String key,
            @RequestParam(value = "page", defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows
    ){

        PageResult<Preferential> result = this.preferentialService.queryPreferentials(key,page,rows);
        if (CollectionUtils.isEmpty(result.getItems())){//CollectionUtils 使用spring中的
            return ResponseEntity.notFound().build();//404
        }
        return ResponseEntity.ok(result);//有值则返回页面集合
    }

}

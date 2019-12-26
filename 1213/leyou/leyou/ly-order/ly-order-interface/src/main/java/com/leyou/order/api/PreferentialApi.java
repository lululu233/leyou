package com.leyou.order.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("preferential")
public interface PreferentialApi {

    @PostMapping("selectPreferential")
    public Integer findPreMoneyByCategoryId(@RequestParam(value = "categoryId") Long categoryId, @RequestParam(value = "money") Long money);

    }

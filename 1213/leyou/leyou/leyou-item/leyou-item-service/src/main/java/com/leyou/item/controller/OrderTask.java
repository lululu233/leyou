package com.leyou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.leyou.item.service.CategoryReportService;
import com.leyou.order.service.OrderService;
//import org.springframework.data.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class OrderTask {

    @Autowired
    private CategoryReportService categoryReportService;
    @Scheduled(cron = "0 0 1 * * ?")
    public void createCategoryReportData(){
        System.out.println("生成数据");
        categoryReportService.createData();
    }

}

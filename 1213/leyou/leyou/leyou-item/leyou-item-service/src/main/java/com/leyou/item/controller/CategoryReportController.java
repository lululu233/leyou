package com.leyou.item.controller;


import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.CategoryReport;
import com.leyou.item.service.CategoryReportService;
import com.leyou.item.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("categoryReport")
public class CategoryReportController {

    @Autowired
    private CategoryReportService categoryReportService;

    /**
     * 昨天的数据统计(测试)
     * @return
     */
    @GetMapping("yesterday")
    public ResponseEntity<List<CategoryReport>> yesterday(){
        LocalDate localDate = LocalDate.now().minusDays(1);// 得到昨天的日期
        List<CategoryReport> categoryReportList = categoryReportService.categoryReport(localDate);
        if (categoryReportList == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(categoryReportList);
    }


    @Autowired
    private CategoryService categoryService;

    /**
     * 统计一级类目
     * @param date1
     * @param date1
     * @return
     */
    @GetMapping("category1Count")
    public ResponseEntity<List<Map>> category1Count(@RequestParam(value = "date1", required = false)String date1 , @RequestParam(value = "date2", required = false)String date2){

        Map map=new HashMap();
        map.put("parentId",0);
        List<Category> categoryList = categoryService.findList(map);//查询一级分类列表
        Map<Long, String> categoryMap = categoryList.stream().collect(Collectors.toMap(Category::getId,Category::getName ));
        //date1="2019-01-26";
        //data2="2019-01-26";
        List<Map> categoryReports = categoryReportService.category1Count(date1, date2);
        /*for(Map  report:categoryReports){
            //根据分类id 查name
            *//*this.categoryService.queryByCategoryNameById((Integer)report.get("categoryId1"));*//*
            String categoryName = categoryMap.get(report.get("categoryName"));
            report.put("categoryName",categoryName);//追加名称
        }*/
        if (categoryReports == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(categoryReports);


    }



}

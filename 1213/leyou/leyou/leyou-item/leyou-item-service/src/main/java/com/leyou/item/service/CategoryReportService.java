package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.mapper.CategoryReportMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.CategoryReport;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CategoryReportService {
    @Autowired
    private CategoryReportMapper categoryReportMapper;


    public List<CategoryReport> categoryReport(LocalDate date) {
        List<CategoryReport> categoryReportList = categoryReportMapper.categoryReport(date);
        return categoryReportList;
    }


    @Transactional
    public void createData() {

        LocalDate localDate = LocalDate.now().minusDays(1);// 得到昨天的日期
        List<CategoryReport> categoryReports = categoryReportMapper.categoryReport(localDate);
        for(CategoryReport categoryReport:categoryReports){
            categoryReportMapper.insert(categoryReport);
        }
    }


    public List<Map> category1Count(String date1, String date2) {
        return categoryReportMapper.category1Count(date1,date2);
    }
}

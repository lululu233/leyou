package com.leyou.item.mapper;


import com.leyou.item.pojo.CategoryReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CategoryReportMapper extends Mapper<CategoryReport> {

    @Select("SELECT category_id1 categoryId1,category_id2 categoryId2,category_id3 categoryId3,DATE_FORMAT(o.`payment_time`,'%Y-%m-%d' ) countDate,SUM(oi.num) num,SUM(oi.money) money " +
            "FROM tb_order_detail  oi, tb_order_status o  " +
            "WHERE  oi.order_id=o.order_id AND o.`status`='2' AND DATE_FORMAT(o.`payment_time`,'%Y-%m-%d' ) =#{date} " +
            "GROUP  BY `category_id1`,`category_id2`,`category_id3`,DATE_FORMAT(o.`payment_time`,'%Y-%m-%d' ) ")
    public List<CategoryReport> categoryReport(@Param("date") LocalDate date);

    /**
     * 按时间段统计一级类目
     * @param date1
     * @param date2
     * @return
     */
    @Select("SELECT category_id1 categoryId1,c.name categoryName,SUM(num) num, SUM(money) money " +
            "FROM tb_category_report r,v_category1 c " +
            "WHERE r.category_id1=c.id AND count_date>=#{date1} AND count_date<=#{date2} GROUP BY category_id1,c.name")
    public List<Map> category1Count(@Param("date1") String date1, @Param("date2") String date2);


}

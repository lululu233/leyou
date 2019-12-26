package com.leyou.order.mapper;


import com.leyou.order.pojo.Preferential;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@org.apache.ibatis.annotations.Mapper
public interface PreferentialMapper extends Mapper<Preferential> {

    List<Preferential> queryPreferentials(@Param("name")String name);
}

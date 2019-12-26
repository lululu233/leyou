package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    /*调用mapper*/
    @Autowired
    private CategoryMapper categoryMapper;



    /**
     * 根据父节点查子节点
     * @param pid
     * @return
     */
    public List<Category> queryCategoryByPid(Long pid) {
        Category recoed = new Category();
        recoed.setParentId(pid);
        return this.categoryMapper.select(recoed);


    }

    public List<String> queryNamesByIds(List<Long> ids){
        List<Category> categories = this.categoryMapper.selectByIdList(ids);
        //转list<String> 把多个category中的 name 组成一个list集合
        return categories.stream().map(category -> category.getName()).collect(Collectors.toList());

    }

    public List<Category> queryByBrandId(Long bid) {
        return this.categoryMapper.queryByBrandId(bid);
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Category> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return categoryMapper.selectByExample(example);
    }
    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 分类名称
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 是否显示
            if(searchMap.get("isShow")!=null && !"".equals(searchMap.get("isShow"))){
                criteria.andLike("isShow","%"+searchMap.get("isShow")+"%");
            }
            // 是否导航
            if(searchMap.get("isMenu")!=null && !"".equals(searchMap.get("isMenu"))){
                criteria.andLike("isMenu","%"+searchMap.get("isMenu")+"%");
            }

            // 分类ID
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }
            // 商品数量
            if(searchMap.get("goodsNum")!=null ){
                criteria.andEqualTo("goodsNum",searchMap.get("goodsNum"));
            }
            // 排序
            if(searchMap.get("seq")!=null ){
                criteria.andEqualTo("seq",searchMap.get("seq"));
            }
            // 级别
            if(searchMap.get("level")!=null ){
                criteria.andEqualTo("level",searchMap.get("level"));
            }
            // 上级ID
            if(searchMap.get("parentId")!=null ){
                criteria.andEqualTo("parentId",searchMap.get("parentId"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }

        }
        return example;
    }

    /**根据一级分类查 name
     * @param categoryId1
     */
    public Category queryByCategoryNameById(Integer categoryId1) {
        return this.categoryMapper.selectByPrimaryKey(categoryId1);

    }
}

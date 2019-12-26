package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    /**根据查询条件分页查询并排序查询品牌信息
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();

        //key  根据name orEqualTo 首字母 模糊查询
        if(StringUtils.isNotBlank(key)){//StringUtils apache 的包
            criteria.andLike("name","%" + key + "%").orEqualTo("letter", key);
        }

        //添加分页条件 page rows
        PageHelper.startPage(page,rows);

        //添加排序条件 sortBy desc
        if (StringUtils.isNotBlank(sortBy)){ //"id desc"  sql模板 字段 空格 desc
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }

        List<Brand> brands = this.brandMapper.selectByExample(example);
        //包装pageInfo 对象
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //包装 分页结果对象返回
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());
    }

    /**
     * 新增品牌
     *
     * @param brand
     * @param cids
     */
    @Transactional /*添加事务会自动判断回滚 则不需要去判断 */
    public void saveBrand(Brand brand, List<Long> cids) {
       //先新增brand flag 标记影响行数
        this.brandMapper.insertSelective(brand);
        //在新增中间表循环遍历
        cids.forEach(cid -> {/*中间表 通用mapper 无法操作 只能自定义方法 sql*/
            this.brandMapper.insertCategoryBrand(cid, brand.getId());
        });

    }

    /**
     * 查询品牌
     * @param cid
     * @return
     */
    public List<Brand> queryBrandsByCid(Long cid) {

        return this.brandMapper.selectBrandsByCid(cid);
    }

    public Brand queryBrandsById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }


    @Transactional
    public int updateBrandByBid(Brand brand, List<Long> cids) {

        //更新 tb_brand
        int i = this.brandMapper.updateByPrimaryKey(brand);

        //修改 tb_category_brand
        cids.forEach(cid -> {/*中间表 通用mapper 无法操作 只能自定义方法 sql*/
            this.brandMapper.updateCategoryBrand(cid, brand.getId());
        });
        return i;
    }
}

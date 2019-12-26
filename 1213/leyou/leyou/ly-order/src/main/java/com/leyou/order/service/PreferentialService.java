package com.leyou.order.service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.order.mapper.PreferentialMapper;
import com.leyou.order.pojo.Preferential;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PreferentialService {

    private static final Logger logger = LoggerFactory.getLogger(PreferentialService.class);

    @Autowired
    private PreferentialMapper preferentialMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Preferential> findAll() {
        return preferentialMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Preferential> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Preferential> preferentials = (Page<Preferential>) preferentialMapper.selectAll();
        return new PageResult<Preferential>(preferentials.getTotal(),preferentials.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Preferential> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return preferentialMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Preferential> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Preferential> preferentials = (Page<Preferential>) preferentialMapper.selectByExample(example);
        return new PageResult<Preferential>(preferentials.getTotal(),preferentials.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Preferential findById(Integer id) {
        return preferentialMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param preferential
     */
    public void add(Preferential preferential) {
        preferentialMapper.insert(preferential);
    }

    /**
     * 修改
     * @param preferential
     */
    public void update(Preferential preferential) {
        preferentialMapper.updateByPrimaryKeySelective(preferential);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(Integer id) {
        preferentialMapper.deleteByPrimaryKey(id);
    }

    public int findPreMoneyByCategoryId(Long categoryId, int money) {

        Example example=new Example(Preferential.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("state","1");//状态
        criteria.andEqualTo("categoryId",categoryId);
        criteria.andLessThanOrEqualTo("buyMoney",money);//小于等于优惠金额
        criteria.andGreaterThanOrEqualTo("endTime",new Date());//截至日期大于等于当前日期
        criteria.andLessThanOrEqualTo("startTime",new Date());//开始日期小于等于当前日期
        example.setOrderByClause("buy_money desc");//按照购买金额降序排序
        List<Preferential> preferentials = preferentialMapper.selectByExample(example);
        if(preferentials.size()>=1){
            Preferential preferential = preferentials.get(0);
            if("1".equals(preferential.getType())){//不翻倍
                return preferential.getPreMoney();//返回优惠的金额
            }else{ //翻倍
                int multiple=  money/preferentials.get(0).getBuyMoney();
                return preferential.getPreMoney()*multiple;
            }
        }else{
            return 0;
        }
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Preferential.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 状态
            if(searchMap.get("state")!=null && !"".equals(searchMap.get("state"))){
                criteria.andLike("state","%"+searchMap.get("state")+"%");
            }
            // 类型1不翻倍 2翻倍
            if(searchMap.get("type")!=null && !"".equals(searchMap.get("type"))){
                criteria.andLike("type","%"+searchMap.get("type")+"%");
            }

            // ID
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }
            // 消费金额
            if(searchMap.get("buyMoney")!=null ){
                criteria.andEqualTo("buyMoney",searchMap.get("buyMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }

        }
        return example;


    }

    /**
     * 根据 优惠名 模糊查询
     * @param key
     * @param page
     * @param rows
     * @return
     */
    public PageResult<Preferential> queryPreferentials(String key, Integer page, Integer rows) {
        //初始化example对象
        Example example = new Example(Preferential.class);
        Example.Criteria criteria = example.createCriteria();

        //key  根据name orEqualTo 首字母 模糊查询
        if(StringUtils.isNotBlank(key)){//StringUtils apache 的包
            criteria.andLike("name","%" + key + "%");
        }

        //添加分页条件 page rows
        PageHelper.startPage(page,rows);



        List<Preferential> brands = this.preferentialMapper.selectByExample(example);
        //包装pageInfo 对象
        PageInfo<Preferential> pageInfo = new PageInfo<>(brands);
        //包装 分页结果对象返回
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());
    }

}

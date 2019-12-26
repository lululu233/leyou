package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 根据查询条件分页查询并排序查询品牌信息
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @GetMapping("page")/*required = false 不知道默认值 设置这个*/
    public ResponseEntity<PageResult<Brand>> queryBrandsByPage(
            @RequestParam(value = "key", required = false)String key,
            @RequestParam(value = "page", defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows,
            @RequestParam(value = "sortBy", required = false)String sortBy,
            @RequestParam(value = "desc", required = false)Boolean desc
    ){

        PageResult<Brand> result = this.brandService.queryBrandsByPage(key,page,rows,sortBy,desc);
        if (CollectionUtils.isEmpty(result.getItems())){//CollectionUtils 使用spring中的
            return ResponseEntity.notFound().build();//404
        }
        return ResponseEntity.ok(result);//有值则返回页面集合
    }

    /** 新增品牌
     * @param brand
     * @param cids
     * @return
     */
    @PostMapping /*不需要放回值 void  接受 json 对象 时只能 创建一个对象 只能传递一个参数 需要分别接受 需要在前端 使用 qs工具*/
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids")List<Long> cids){
        this.brandService.saveBrand(brand, cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();//201

    }

    /**
     * 查询品牌
     * @param cid
     * @return
     */
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandsBycid(@PathVariable("cid")Long cid){
        List<Brand> brands = this.brandService.queryBrandsByCid(cid);
        if (CollectionUtils.isEmpty(brands)){//CollectionUtils 使用spring中的
            return ResponseEntity.notFound().build();//404
        }
        return ResponseEntity.ok(brands);
    }

    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id")Long id){
        Brand brand = this.brandService.queryBrandsById(id);
        if (brand == null){
            return ResponseEntity.notFound().build();

        }
        return ResponseEntity.ok(brand);
    }

    /** 更新品牌
     * @param brand
     * @param cids
     * @return
     */
    @PutMapping
    public ResponseEntity<Integer> updateBrandByBid(Brand brand, @RequestParam("cids")List<Long> cids){
        Integer i = (Integer) this.brandService.updateBrandByBid(brand, cids);
        if (i == 0){
            return ResponseEntity.notFound().build();

        }
        return ResponseEntity.ok(i);
    }

    @GetMapping("delete/{bid}")
    public ResponseEntity<Integer> deleteBrandById(){
        return null;
    }



}

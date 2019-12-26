package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {

    /*调用service*/
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点 pid 查 子节点
     * @param pid
     * @return
     */
    /*编写业务 rest 风格*/
    @GetMapping("list")                                                           /* 防止参数为空 设置默认值 0表示一级 目录*/
    public ResponseEntity<List<Category>> queryCategoriesByPid(@RequestParam(value = "pid",defaultValue = "0")Long pid) {

        if (pid == null || pid < 0) {
                /*400请求参数不合法*/
            //return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); 简化成下面
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = this.categoryService.queryCategoryByPid(pid);
        /*判断是否为空 */
        if (CollectionUtils.isEmpty(categories)) {
            //为空404
            //return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            return ResponseEntity.notFound().build();
        }
        //找到 正常响应 200
        return ResponseEntity.ok(categories);
    }

    //返回分类名称
    @GetMapping
    public ResponseEntity<List<String>> queryNamesByIds(@RequestParam("ids")List<Long> ids){
        List<String> names = this.categoryService.queryNamesByIds(ids);

        if (CollectionUtils.isEmpty(names)) {
            //为空404
            //return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            return ResponseEntity.notFound().build();
        }
        //找到 正常响应 200
        return ResponseEntity.ok(names);
    }

    /**
     * 通过品牌id查询商品分类
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid) {
        List<Category> list = this.categoryService.queryByBrandId(bid);
        if (list == null || list.size() < 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }

}

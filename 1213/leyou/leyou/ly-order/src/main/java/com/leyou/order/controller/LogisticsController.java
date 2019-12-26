package com.leyou.order.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.order.pojo.Logistics;
import com.leyou.order.pojo.LogisticsB;
import com.leyou.order.pojo.Preferential;
import com.leyou.order.service.LogisticsService;
import com.leyou.order.service.PreferentialService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("logistics")
@Api("物流管理")
public class LogisticsController {

    @Autowired
    private LogisticsService logisticsService;

    /**查看物流商
     * @return
     */
    @GetMapping("selectLogisticsB")
    public ResponseEntity<List<LogisticsB>> selectLogisticsB() {
        List<LogisticsB> results = logisticsService.selectLogisticsB();
        if (results == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(results);

    }

    /**分页
     * 查看物流单
     * @return
     */
    @GetMapping("queryLogistics")
    public ResponseEntity<PageResult<Logistics>> queryLogistics(@RequestParam(value = "key", required = false)String key,
                                                          @RequestParam(value = "page", defaultValue = "1")Integer page,
                                                          @RequestParam(value = "rows", defaultValue = "5")Integer rows
    ) {
        PageResult<Logistics> results = logisticsService.queryLogistics(key,page,rows);
        if (results == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(results);

    }

    /**分页
     * 查看物流商
     * @return
     */
    @GetMapping("queryLogisticsB")
    public ResponseEntity<PageResult<LogisticsB>> queryLogisticsB(@RequestParam(value = "key", required = false)String key,
                                                                @RequestParam(value = "page", defaultValue = "1")Integer page,
                                                                @RequestParam(value = "rows", defaultValue = "5")Integer rows
    ) {
        PageResult<LogisticsB> results = logisticsService.queryLogisticsB(key,page,rows);
        if (results == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(results);

    }

    /**
     * 创建物流
     *
     * @param
     * @return
     */
    @GetMapping("createLogistics")
    public ResponseEntity<Long> createLogistics(@RequestParam(value = "orderId") Long orderId,
                                                @RequestParam(value = "select") Integer select,
                                                @RequestParam(value = "logisticsId") Long logisticsId) {

        Long result = this.logisticsService.createLogistics(orderId,select,logisticsId);
        if (result == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);


    }

}

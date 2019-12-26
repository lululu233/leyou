package com.leyou.order.client;


import com.leyou.seckill.api.SerckillOrderApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "seckill-service")
public interface SeckillOrderClient extends SerckillOrderApi {
}

package com.leyou.cart.client;

import com.leyou.order.api.PreferentialApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("order-service")
public interface PreferentialClient extends PreferentialApi {
}

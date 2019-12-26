package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@EnableScheduling
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.leyou.seckill.mapper") /*使用通用mapper tk包*/
public class LeyouSeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeyouSeckillApplication.class);
    }

}

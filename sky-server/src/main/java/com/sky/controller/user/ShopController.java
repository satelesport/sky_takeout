package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userShopController")
@RequestMapping("/user/shop")
public class ShopController {

    static final String KEY = "SHOP_STATUS";

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 查询店铺状态
     * @return
     */
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        log.info("查询店铺状态");
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Integer status = (Integer) valueOperations.get(KEY);
        return Result.success(status);
    }

}

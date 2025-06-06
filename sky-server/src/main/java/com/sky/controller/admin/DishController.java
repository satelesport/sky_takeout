package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Slf4j
@Api("菜品管理接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}", dishDTO);

        modifyRedis(dishDTO.getCategoryId());

        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询菜品：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除: {}", ids);

        //所有菜品缓存数据都清理掉
        modifyRedisAll();

        dishService.delete(ids);
        return Result.success();
    }

    /**
     * 根据Id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> queryById(@PathVariable Long id){
        log.info("根据id查询菜品：{}", id);
        DishVO dishVO = dishService.queryById(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result modifyDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品: {}", dishDTO);

        //因为可能修改分类 可能影响到两个分类
        modifyRedisAll();

        dishService.modifyDish(dishDTO);
        return Result.success();
    }

    /**
     * 起售停售菜品
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("起售停售菜品")
    @PostMapping("/status/{status}")
    public Result activeOrStop(@PathVariable Integer status, Long id){

        modifyRedisAll();

        dishService.activeOrStop(id, status);
        return Result.success();
    }

    /**
     * 根据分类查询菜品
     */
    @ApiOperation("根据分类查询菜品")
    @GetMapping("/list")
    public Result<List<Dish>> queryByCategory(Long categoryId, String name){
        log.info("根据分类查询菜品：{}, {}", categoryId, name);
        List<Dish> dishList = dishService.queryByCategory(categoryId, name);
        return Result.success(dishList);
    }


    /**
     * 清理一种缓存数据
     * @param categoryId
     */
    private void modifyRedis(Long categoryId){
        String key = "dish_" + categoryId;
        redisTemplate.delete(key);
    }

    /**
     * 清理所有缓存数据
     */
    private void modifyRedisAll(){
        Set<String> keys = redisTemplate.keys("dish_*");
        if(keys != null){
            redisTemplate.delete(keys);
        }
    }
}

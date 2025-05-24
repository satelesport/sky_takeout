package com.sky.mapper;

import com.sky.annotation.AutoFillAnnotation;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品ID查询关联了多少套餐
     * @param dishId
     * @return
     */
    @Select("select count(0) from setmeal_dish where dish_id = #{dishId}")
    public Integer countByDishId(Long dishId);

    @AutoFillAnnotation(OperationType.INSERT)
    void save(List<SetmealDish> setmealDishes);
}

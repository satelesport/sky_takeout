package com.sky.mapper;

import com.sky.annotation.AutoFillAnnotation;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 新增套餐
     * @param setmeal
     */
    @AutoFillAnnotation(OperationType.INSERT)
    void save(Setmeal setmeal);

    List<SetmealVO> queryByName(SetmealPageQueryDTO setmealPageQueryDTO);

    @Select("select * from setmeal where id = #{id}")
    Setmeal queryById(Long id);


    void deleteByIds(List<Long> ids);

    @AutoFillAnnotation(OperationType.UPDATE)
    void modify(Setmeal setmeal);
}

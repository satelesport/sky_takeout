package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 查询是否已有这个商品
     * @param shoppingCart
     * @return
     */
    ShoppingCart query(ShoppingCart shoppingCart);

    /**
     * 更新商品数量
     * @param qShoppingCart
     */
    void update(ShoppingCart qShoppingCart);

    /**
     * 新增商品
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void save(ShoppingCart shoppingCart);

    /**
     * 查询购物车中商品
     * @param userId
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> list(Long userId);

    /**
     * 删除购物车中一项/所有
     * @param shoppingCart
     */
    void delete(ShoppingCart shoppingCart);
}

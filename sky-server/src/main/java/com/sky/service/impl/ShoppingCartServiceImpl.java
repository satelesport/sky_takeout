package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    DishMapper dishMapper;

    /**
     * 新增购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO){
        //需要先判断商品是否存在于购物车 如果存在数量加一 不存在则新增商品
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ShoppingCart qShoppingCart = shoppingCartMapper.query(shoppingCart);
        if(qShoppingCart != null){
            qShoppingCart.setNumber(qShoppingCart.getNumber() + 1);
            shoppingCartMapper.update(qShoppingCart);
        }
        else{
            shoppingCart.setAmount(new BigDecimal(1));
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            if(shoppingCart.getDishId() != null){
                Dish dish = dishMapper.getByID(shoppingCart.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
            }
            else{
                Setmeal setmeal = setmealMapper.queryById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }
            shoppingCartMapper.save(shoppingCart);
        }
    }

    /**
     * 查询购物车商品
     * @return
     */
    @Override
    public List<ShoppingCart> list(){
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(BaseContext.getCurrentId());
        return shoppingCartList;
    }

    /**
     * 清空购物车
     */
    @Override
    public void deleteAll(){
        ShoppingCart shoppingCart = ShoppingCart.builder()
                        .userId(BaseContext.getCurrentId())
                                .build();
        shoppingCartMapper.delete(shoppingCart);
    }

    /**
     * 购物车中一项数量减一
     * @param shoppingCartDTO
     */
    @Override
    public void delete(ShoppingCartDTO shoppingCartDTO){
        //先判断当前商品数量是否为一 如果不是就更新数量 否则直接删除
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ShoppingCart qShoppingCart = shoppingCartMapper.query(shoppingCart);
        if(qShoppingCart.getNumber() != 1){
            qShoppingCart.setNumber(qShoppingCart.getNumber() - 1);
            shoppingCartMapper.update(qShoppingCart);
        }
        else{
            shoppingCartMapper.delete(qShoppingCart);
        }
    }
}

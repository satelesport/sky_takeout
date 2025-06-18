package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderSubmitVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("insert into orders (number, status, user_id, address_book_id, order_time, pay_method, pay_status, amount, remark, phone, address, consignee, estimated_delivery_time, delivery_status, pack_amount, tableware_number, tableware_status) values " +
            "(#{number}, #{status}, #{userId}, #{addressBookId}, #{orderTime}, #{payMethod}, #{payStatus}, #{amount}, #{remark}, #{phone}, #{address}, #{consignee}, #{estimatedDeliveryTime}, #{deliveryStatus}, #{packAmount}, #{tablewareNumber}, #{tablewareStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 按条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    List<Orders> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询特定状态的订单
     * @param
     * @return
     */
    @Select("select count(0) from orders where status = #{status}")
    Integer statistics(Integer status);

    /**
     * 根据id查询
     */
    @Select("select * from orders where id = #{id}")
    Orders queryById(Long id);


    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> taskQuery(LocalDateTime time, Integer status);

    /**
     * 根据起始时间查询营业额
     * @return
     */
    Double queryByDay(LocalDateTime begin, LocalDateTime end, Integer status);
}

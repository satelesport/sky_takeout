package com.sky.mapper;

import com.sky.entity.Orders;
import com.sky.vo.OrderSubmitVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface OrderMapper {

    @Insert("insert into orders (number, status, user_id, address_book_id, order_time, pay_method, pay_status, amount, remark, phone, address, consignee, estimated_delivery_time, delivery_status, pack_amount, tableware_number, tableware_status) values " +
            "(#{number}, #{status}, #{userId}, #{addressBookId}, #{orderTime}, #{payMethod}, #{payStatus}, #{amount}, #{remark}, #{phone}, #{address}, #{consignee}, #{estimatedDeliveryTime}, #{deliveryStatus}, #{packAmount}, #{tablewareNumber}, #{tablewareStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(Orders orders);
}

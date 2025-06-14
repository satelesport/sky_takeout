package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    OrderMapper orderMapper;

    /**
     * 每分钟检查一次是否有过期订单
     */
    @Scheduled(cron = "0 * * * * *")
    public void timeoutOrder(){
        List<Orders> ordersList = orderMapper.taskQuery(LocalDateTime.now().minusMinutes(15), Orders.PENDING_PAYMENT);

        if(ordersList != null && !ordersList.isEmpty()){
            for(Orders orders : ordersList){
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时");
                orderMapper.update(orders);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void deliveryOrder(){
        List<Orders> ordersList = orderMapper.taskQuery(LocalDateTime.now(), Orders.DELIVERY_IN_PROGRESS);

        if(ordersList != null && !ordersList.isEmpty()){
            for(Orders orders : ordersList){
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}

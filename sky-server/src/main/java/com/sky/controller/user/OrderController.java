package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/user/order")
@RestController("userOrderController")
@Slf4j
@Api("订单相关接口")
public class OrderController {

    @Autowired
    OrderService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
//    @PutMapping("/payment")
//    @ApiOperation("订单支付")
//    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        log.info("订单支付：{}", ordersPaymentDTO);
//        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
//        log.info("生成预支付交易单：{}", orderPaymentVO);
//        return Result.success(orderPaymentVO);
//    }
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<String> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return  Result.success();
    }

    /**
     * 查询历史订单
     * @return
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrders(int page, int pageSize, Integer status){
        PageResult pageResult = orderService.historyOrders(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     * @param orderId
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> orderDetail(@PathVariable("id") Long orderId){
        OrderVO orderVO = orderService.details(orderId);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable("id") Long orderId){
        OrdersCancelDTO ordersCancelDTO = new OrdersCancelDTO();
        ordersCancelDTO.setId(orderId);
        ordersCancelDTO.setCancelReason("用户取消");
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 再来一单
     * @param orderId
     * @return
     */
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable("id") Long orderId){
        orderService.repetition(orderId);
        return Result.success();
    }

    /**
     * 用户催单
     * @param orderId
     * @return
     */
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable("id") Long orderId){
        orderService.reminder(orderId);
        return Result.success();
    }
}

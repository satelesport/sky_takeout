package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderMapper orderMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    ShoppingCartMapper shoppingCartMapper;
    @Autowired
    AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO){
        //处理业务异常：地址簿为空 购物车为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(BaseContext.getCurrentId());
        if(shoppingCartList == null || shoppingCartList.isEmpty()){
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        //向订单表插入一条数据
        orderMapper.save(orders);
        Long orderId = orders.getId();

        //向订单明细表插入多条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart shoppingCart : shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orderId);
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.save(orderDetailList);

        //清空购物车 返回VO
        ShoppingCart shoppingCart = ShoppingCart.builder()
                        .userId(BaseContext.getCurrentId())
                                .build();
        shoppingCartMapper.delete(shoppingCart);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orderId)
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 按条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = (Page<Orders>) orderMapper.conditionSearch(ordersPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 返回各个状态的订单
     * @return
     */
    @Override
    public OrderStatisticsVO statistics(){
        //返回待派送3 派送中4 待接单2三种状态
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(orderMapper.statistics(Orders.CONFIRMED));
        orderStatisticsVO.setToBeConfirmed(orderMapper.statistics(Orders.TO_BE_CONFIRMED));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.statistics(Orders.DELIVERY_IN_PROGRESS));
        return orderStatisticsVO;
    }

    /**
     * 订单详细查询
     * @param orderId
     * @return
     */
    @Override
    public OrderVO details(Long orderId){
        Orders orders = orderMapper.queryById(orderId);
        List<OrderDetail> orderDetailList =  orderDetailMapper.queryByOrderId(orderId);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 接单
     * @param
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO){
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param
     */
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        //只有处于待接单的订单才能拒单 如果已经支付了则需要退款
        Orders qOrders = orderMapper.queryById(ordersRejectionDTO.getId());
        if(qOrders == null || !(Objects.equals(qOrders.getStatus(), Orders.TO_BE_CONFIRMED))){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

//        //支付状态
//        Integer payStatus = qOrders.getPayStatus();
//        if (payStatus == Orders.PAID) {
//            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    qOrders.getNumber(),
//                    qOrders.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders orders = new Orders();
        orders.setId(qOrders.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO){
        //如果已经支付了则需要退款
        Orders qOrders = orderMapper.queryById(ordersCancelDTO.getId());

//        //支付状态
//        Integer payStatus = qOrders.getPayStatus();
//        if (payStatus == Orders.PAID) {
//            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    qOrders.getNumber(),
//                    qOrders.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//        }

        // 取消需要退款，根据订单id更新订单状态、原因、时间
        Orders orders = new Orders();
        orders.setId(qOrders.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param orderId
     */
    @Override
    public void delivery(Long orderId){
        //要检测用户是否付款 只有接单的才可以派送
        Orders qOrders = orderMapper.queryById(orderId);
        if(!qOrders.getPayStatus().equals(Orders.PAID) || !qOrders.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param orderId
     */
    @Override
    public void complete(Long orderId){
        //只有派送中的才可以完成
        Orders qOrders = orderMapper.queryById(orderId);
        if(!qOrders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 查询历史订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult historyOrders(int page, int pageSize, Integer status){
        PageHelper.startPage(page, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        Page<Orders> ordersPage = (Page<Orders>) orderMapper.conditionSearch(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();
        if(ordersPage != null && !ordersPage.isEmpty()){
            for(Orders orders : ordersPage){
                Long ordersId = orders.getId();
                List<OrderDetail> orderDetailList = orderDetailMapper.queryByOrderId(ordersId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                orderVOList.add(orderVO);
            }
        }

        return new PageResult(ordersPage.getTotal(), orderVOList);
    }

    /**
     * 再来一单
     * @param orderId
     */
    @Override
    public void repetition(Long orderId){
        //把指定订单的数据拷贝到当前购物车中
        List<OrderDetail> orderDetailList = orderDetailMapper.queryByOrderId(orderId);

        for(OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.save(shoppingCart);
        }
    }
}

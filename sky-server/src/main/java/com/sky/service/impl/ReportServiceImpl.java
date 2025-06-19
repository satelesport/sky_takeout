package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    UserMapper userMapper;

    /**
     * 查询营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end){
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        //存放起止日期之间的日期
        List<LocalDate> dayList = new ArrayList<>();

        dayList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dayList.add(begin);
        }

        turnoverReportVO.setDateList(StringUtils.join(dayList, ','));

        List<Double> amountList = new ArrayList<>();
        for(LocalDate day : dayList){
            LocalDateTime beginTime = LocalDateTime.of(day, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(day, LocalTime.MAX);

            Double turnover = orderMapper.queryByDay(beginTime, endTime, Orders.COMPLETED);
            if(turnover == null){
                turnover = 0.0;
            }
            amountList.add(turnover);
        }

        turnoverReportVO.setTurnoverList(StringUtils.join(amountList, ','));
        return turnoverReportVO;
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end){
        //存放起止日期之间的日期
        List<LocalDate> dayList = new ArrayList<>();

        dayList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dayList.add(begin);
        }

        List<Long> newUser = new ArrayList<>();
        List<Long> totalUser = new ArrayList<>();
        Long total = 0L;
        for(LocalDate day : dayList){
            LocalDateTime beginTime = LocalDateTime.of(day, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(day, LocalTime.MAX);

            Long User = userMapper.queryByDay(beginTime, endTime);
            if(User == null){
                User = 0L;
            }
            newUser.add(User);
            total += User;
            totalUser.add(total);
        }

        UserReportVO userReportVO = UserReportVO.builder()
                .dateList(StringUtils.join(dayList, ','))
                .newUserList(StringUtils.join(newUser, ','))
                .totalUserList(StringUtils.join(totalUser, ','))
                .build();

        return userReportVO;
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end){
        List<LocalDate> dayList = new ArrayList<>();
        dayList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dayList.add(begin);
        }

        List<Long> newOrder = new ArrayList<>();
        List<Long> validOrder = new ArrayList<>();
        Long total = 0L;
        Long validTotal = 0L;
        for(LocalDate day : dayList){
            LocalDateTime beginTime = LocalDateTime.of(day, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(day, LocalTime.MAX);

            Long order = orderMapper.queryOrderByDay(beginTime, endTime, null);
            Long valid = orderMapper.queryOrderByDay(beginTime, endTime, Orders.COMPLETED);

            order = order == null ? 0 : order;
            valid = valid == null ? 0 : valid;

            total += order;
            validTotal += valid;

            newOrder.add(order);
            validOrder.add(valid);
        }

        Double orderCompletionRate  = 0.0;
        if(total != 0L){
            orderCompletionRate = validTotal.doubleValue() / total.doubleValue();
        }

        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(StringUtils.join(dayList, ','))
                .orderCountList(StringUtils.join(newOrder, ','))
                .validOrderCountList(StringUtils.join(validOrder, ','))
                .totalOrderCount(total.intValue())
                .validOrderCount(validTotal.intValue())
                .orderCompletionRate(orderCompletionRate)
                .build();
        return orderReportVO;
    }

    /**
     * 统计top10菜品
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end){
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.top10();
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        for(GoodsSalesDTO goodsSalesDTO : goodsSalesDTOList){
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber());
        }
        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .numberList(StringUtils.join(numberList, ','))
                .nameList(StringUtils.join(nameList, ','))
                .build();
        return salesTop10ReportVO;
    }
}

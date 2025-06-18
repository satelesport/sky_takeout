package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    OrderMapper orderMapper;

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
}

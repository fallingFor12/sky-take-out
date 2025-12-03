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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定时间区间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end)      {
        //存放开始到结束的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            //日期计算，获取开始日期的下一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        String dateListStr = StringUtils.join(dateList, ",");
        //存放每天营业额数据
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            //获取指定日期的营业额数据(状态应该为已完成)
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);

            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        String turnoverListStr = StringUtils.join(turnoverList, ",");
        return new TurnoverReportVO(dateListStr, turnoverListStr);
    }

    /**
     * 统计指定时间区间内的用户数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //存放开始到结束的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            //日期计算，获取开始日期的下一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        String dateListStr = StringUtils.join(dateList, ",");

        //存放每天总用户数据
        List<Integer> totalList = new ArrayList<>();
        //存放每天新增用户的数量
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //获取指定日期的用户数据
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("end", endTime);
            //截止今天之前的所有用户
            Integer totalUser = userMapper.countByMap(map);
            totalList.add(totalUser);
            map.put("begin", beginTime);
            //今天内的新用户
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);
        }
        String totalListStr = StringUtils.join(totalList, ",");
        String newUserListStr = StringUtils.join(newUserList, ",");
        return new UserReportVO(dateListStr, totalListStr, newUserListStr);
    }

    /**
     * 统计指定时间区间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //存放开始到结束的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            //日期计算，获取开始日期的下一天
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        String dateListStr = StringUtils.join(dateList, ",");

        //订单总数(也可以根据stream流求和)
        Integer totalOrderCount = 0;
        //有效订单总数
        Integer validOrderCount = 0;
        //存放每天订单数量
        List<Integer> orderCountList = new ArrayList<>();
        //存放每天有效订单数量
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //获取指定日期的用户数据
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin", beginTime);
            map.put("end", endTime);
            //查询每天订单数量（select count(id) from orders where order_time < ? and order_time > ?）
            Integer Count = orderMapper.countByMap(map);
            map.put("status", Orders.COMPLETED);
            //查询每天有效订单数量（select count(id) from orders where order_time < ? and order_time > ? and status = complete）
            Integer validCount = orderMapper.countByMap(map);
            totalOrderCount = totalOrderCount + Count;
            validOrderCount = validOrderCount + validCount;
            orderCountList.add(Count);
            validOrderCountList.add(validCount);
        }
        //stream流求和
//        Integer i = orderCountList.stream().reduce(Integer::sum).get();
        String orderCountListStr = StringUtils.join(orderCountList, ",");
        String validOrderCountListStr = StringUtils.join(validOrderCountList, ",");
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return new OrderReportVO(dateListStr,orderCountListStr,validOrderCountListStr,totalOrderCount,validOrderCount,orderCompletionRate);
    }

    /**
     * 统计指定时间区间内的销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        //stream流处理数据
        //存放商品名称
        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameListStr = StringUtils.join(nameList, ",");
        //存放商品销量
        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberListStr = StringUtils.join(numberList, ",");
        return new SalesTop10ReportVO(nameListStr,numberListStr);
    }
}

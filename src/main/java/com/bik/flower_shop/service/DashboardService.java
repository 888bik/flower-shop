package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bik.flower_shop.enumeration.PayStatusEnum;
import com.bik.flower_shop.enumeration.ShipStatusEnum;
import com.bik.flower_shop.enumeration.RefundStatusEnum;
import com.bik.flower_shop.mapper.GoodsMapper;
import com.bik.flower_shop.mapper.OrdersMapper;
import com.bik.flower_shop.mapper.UserMapper;
import com.bik.flower_shop.pojo.dto.DailyCountDTO;
import com.bik.flower_shop.pojo.dto.HourlyCountDTO;
import com.bik.flower_shop.pojo.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrdersMapper ordersMapper;
    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;

    public DashboardStatusVO getStatus() {

        DashboardStatusVO vo = new DashboardStatusVO();

        // 商品统计
        List<StatusCountVO> goods = new ArrayList<>();

        goods.add(new StatusCountVO("审核中",
                goodsMapper.countPending()));

        goods.add(new StatusCountVO("销售中",
                goodsMapper.countOnSale()));

        goods.add(new StatusCountVO("已下架",
                goodsMapper.countOffSale()));

        goods.add(new StatusCountVO("库存预警",
                goodsMapper.countLowStock()));

        vo.setGoods(goods);

        // 订单统计（基于原子字段）
        List<StatusCountVO> orders = new ArrayList<>();

        // 待付款：pay_status = unpaid 且 未被管理员删除
        orders.add(new StatusCountVO("待付款",
                ordersMapper.countByPayStatus(PayStatusEnum.UNPAID.getCode())));

        // 待发货：已支付且当前为待发货
        orders.add(new StatusCountVO("待发货",
                ordersMapper.countPaidAndShipPending()));

        // 已发货：ship_status = shipped
        orders.add(new StatusCountVO("已发货",
                ordersMapper.countByShipStatus(ShipStatusEnum.SHIPPED.getCode())));

        // 退款中：refund_status = pending
        orders.add(new StatusCountVO("退款中",
                ordersMapper.countByRefundStatus(RefundStatusEnum.PENDING.getCode())));

        vo.setOrder(orders);

        return vo;
    }

    public DashboardPanelResponseVO getPanels() {

        List<DashboardPanelVO> panels = new ArrayList<>();

        // 支付订单：统计 pay_status = paid（排除被管理员删除）
        long paidOrderCount = ordersMapper.countByPayStatus(PayStatusEnum.PAID.getCode());

        DashboardPanelVO paidPanel = new DashboardPanelVO();
        paidPanel.setTitle("支付订单");
        paidPanel.setValue(paidOrderCount);
        paidPanel.setUnit("年");
        paidPanel.setUnitColor("success");
        paidPanel.setSubTitle("总支付订单");
        paidPanel.setSubValue(paidOrderCount);
        paidPanel.setSubUnit("");
        panels.add(paidPanel);

        // 订单量 + 转化率：总订单（未被管理员删除）
        long totalOrderCount = ordersMapper.selectCount(
                new LambdaQueryWrapper<com.bik.flower_shop.pojo.entity.Orders>()
                        .eq(com.bik.flower_shop.pojo.entity.Orders::getDeletedByAdmin, 0)
        );

        String conversionRate = totalOrderCount == 0
                ? "0%"
                : (paidOrderCount * 100 / totalOrderCount) + "%";

        DashboardPanelVO orderPanel = new DashboardPanelVO();
        orderPanel.setTitle("订单量");
        orderPanel.setValue(totalOrderCount);
        orderPanel.setUnit("周");
        orderPanel.setUnitColor("danger");
        orderPanel.setSubTitle("转化率");
        orderPanel.setSubValue(conversionRate);
        orderPanel.setSubUnit("");
        panels.add(orderPanel);

        // 销售额：汇总已支付且未最终退款的订单总额
        BigDecimal totalSales = ordersMapper.sumTotalSales();

        DashboardPanelVO salesPanel = new DashboardPanelVO();
        salesPanel.setTitle("销售额");
        salesPanel.setValue(totalSales);
        salesPanel.setUnit("年");
        salesPanel.setUnitColor("");
        salesPanel.setSubTitle("总销售额");
        salesPanel.setSubValue(totalSales);
        salesPanel.setSubUnit("");
        panels.add(salesPanel);

        // 新增用户
        long userCount = userMapper.selectCount(null);

        DashboardPanelVO userPanel = new DashboardPanelVO();
        userPanel.setTitle("新增用户");
        userPanel.setValue(userCount);
        userPanel.setUnit("年");
        userPanel.setUnitColor("warning");
        userPanel.setSubTitle("总用户");
        userPanel.setSubValue(userCount);
        userPanel.setSubUnit("人");
        panels.add(userPanel);

        DashboardPanelResponseVO resp = new DashboardPanelResponseVO();
        resp.setPanels(panels);

        return resp;
    }

    public DashboardTrendVO getRecentOrderTrend(String type) {

        return switch (type.toLowerCase()) {
            case "hour" -> buildHourTrend();
            case "month" -> buildDayTrend(30);
            default -> buildDayTrend(7);
        };
    }

    private DashboardTrendVO buildDayTrend(int days) {

        // 最近 N 天（倒序）
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            dates.add(LocalDate.now().minusDays(i));
        }

        // 数据库统计
        List<DailyCountDTO> dbList = ordersMapper.countRecentDays(days);

        Map<String, Long> countMap = dbList.stream()
                .collect(Collectors.toMap(
                        DailyCountDTO::getDay,
                        DailyCountDTO::getCnt
                ));

        List<String> x = new ArrayList<>();
        List<Long> y = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (LocalDate date : dates) {
            String key = date.format(formatter);
            x.add(key);
            y.add(countMap.getOrDefault(key, 0L));
        }

        DashboardTrendVO vo = new DashboardTrendVO();
        vo.setX(x);
        vo.setY(y);
        return vo;
    }

    private DashboardTrendVO buildHourTrend() {

        // 最近 24 小时（倒序：00, 23, 22 ...）
        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%02d", i));
        }

        // 查数据库
        List<HourlyCountDTO> dbList = ordersMapper.countTodayByHour();

        Map<String, Long> countMap = dbList.stream()
                .collect(Collectors.toMap(
                        HourlyCountDTO::getHour,
                        HourlyCountDTO::getCnt
                ));

        List<String> x = new ArrayList<>();
        List<Long> y = new ArrayList<>();


        for (int i = hours.size() - 1; i >= 0; i--) {
            String h = hours.get(i);
            x.add(h);
            y.add(countMap.getOrDefault(h, 0L));
        }

        DashboardTrendVO vo = new DashboardTrendVO();
        vo.setX(x);
        vo.setY(y);
        return vo;
    }

}

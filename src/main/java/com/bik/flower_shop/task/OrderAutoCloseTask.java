package com.bik.flower_shop.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bik.flower_shop.enumeration.PayStatusEnum;
import com.bik.flower_shop.mapper.OrdersMapper;
import com.bik.flower_shop.pojo.entity.Orders;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * @author bik
 */
@Component
@RequiredArgsConstructor
public class OrderAutoCloseTask {

    private final OrdersMapper ordersMapper;

    /**
     * 每分钟执行一次，关闭超时未支付订单
     */
    @Scheduled(fixedRate = 60_000)
    public void closeTimeoutOrders() {
        int now = (int) Instant.now().getEpochSecond();
        // 30分钟
        int timeout = 30 * 60;

        List<Orders> orders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getPayStatus, PayStatusEnum.UNPAID.getCode())
                        .eq(Orders::getClosed, false)
                        .lt(Orders::getCreateTime, now - timeout)
        );

        for (Orders order : orders) {
            order.setClosed(true);
            order.setUpdateTime(now);
            order.setPayStatus(PayStatusEnum.CLOSED.getCode());
            ordersMapper.updateById(order);
        }
    }
}

package com.bik.flower_shop.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bik.flower_shop.enumeration.PayStatusEnum;
import com.bik.flower_shop.mapper.CouponUserMapper;
import com.bik.flower_shop.mapper.GoodsMapper;
import com.bik.flower_shop.mapper.OrderItemMapper;
import com.bik.flower_shop.mapper.OrdersMapper;
import com.bik.flower_shop.pojo.entity.CouponUser;
import com.bik.flower_shop.pojo.entity.OrderItem;
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
    private final OrderItemMapper orderItemMapper;
    private final GoodsMapper goodsMapper;
    private final CouponUserMapper couponUserMapper;


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

            // 回滚库存
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            for (OrderItem item : items) {
                goodsMapper.increaseStock(item.getGoodsId(), item.getNum());
            }
            order.setClosed(true);
            order.setUpdateTime(now);
            ordersMapper.updateById(order);

            // 恢复优惠券
            if (order.getCouponUserId() != null) {
                CouponUser cu = couponUserMapper.selectById(order.getCouponUserId());
                if (cu != null && cu.getUsed() != null && cu.getUsed() == 1) {
                    cu.setUsed((byte) 0);
                    couponUserMapper.updateById(cu);
                }
            }

            // 关闭订单（幂等）
            order.setClosed(true);
            order.setUpdateTime(now);
            ordersMapper.updateById(order);

        }
    }
}

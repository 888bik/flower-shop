package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bik.flower_shop.mapper.CartMapper;
import com.bik.flower_shop.mapper.GoodsMapper;
import com.bik.flower_shop.mapper.OrderItemMapper;
import com.bik.flower_shop.mapper.OrderMapper;
import com.bik.flower_shop.pojo.dto.OrderCreateDTO;
import com.bik.flower_shop.pojo.dto.OrderCreateItemDTO;
import com.bik.flower_shop.pojo.entity.Cart;
import com.bik.flower_shop.pojo.entity.Order;
import com.bik.flower_shop.pojo.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * @author bik
 */
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    //    private final GoodsSkuMapper goodsSkuMapper;
    private final GoodsMapper goodsMapper;

    /**
     * 创建订单，返回 orderId 或订单对象
     */
    @Transactional
    public Order createOrder(Integer userId, OrderCreateDTO dto) {
        // 1. 校验 items 非空
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单项为空");
        }

        // 2. 遍历 items，查询单价（服务端决定价格，客户端不可信）
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        int now = (int) Instant.now().getEpochSecond();

        for (OrderCreateItemDTO it : dto.getItems()) {
            BigDecimal unitPrice = fetchSkuPrice(it.getGoodsId(), it.getSkuId());
            if (unitPrice == null) {
                throw new IllegalArgumentException("找不到商品或 SKU：" + it.getGoodsId() + "/" + it.getSkuId());
            }
            BigDecimal numBig = BigDecimal.valueOf(it.getNum());
            BigDecimal sub = unitPrice.multiply(numBig);
            total = total.add(sub);

            OrderItem orderItem = new OrderItem();
            orderItem.setGoodsId(it.getGoodsId());
            orderItem.setNum(it.getNum());
            orderItem.setPrice(unitPrice);
            orderItem.setCreateTime(now);
            // skusType/skuId/goodsNum 可根据你的业务填充
            orderItem.setSkusType((byte) 0);
            orderItem.setGoodsNum(it.getNum());
            // userId set later when inserting or can set now
            orderItem.setUserId(userId);

            items.add(orderItem);
        }

        // TODO: 若有优惠券、运费、税费，需要在这里扣减/计算

        // 3. 构造 Order 实体并插入
        Order order = new Order();
        order.setNo(genOrderNo());
        order.setUserId(userId);
        order.setAddress(dto.getAddress());
        order.setTotalPrice(total);
        order.setRemark(dto.getRemark());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setClosed(false);
        order.setReviewed(false);
        order.setShipStatus("pending");
        order.setCouponUserId(dto.getCouponUserId());

        orderMapper.insert(order);

        // 4. 插入 order items（设置 orderId）
        for (OrderItem oi : items) {
            oi.setOrderId(order.getId());
            oi.setCreateTime(now);
            orderItemMapper.insert(oi);
        }

        // 5. 清理购物车（如果这些订单项来源于购物车）
        // 我们尽量删除同 user & goodsId & skuId 的购物车项
        for (OrderCreateItemDTO it : dto.getItems()) {
            LambdaQueryWrapper<Cart> q = new LambdaQueryWrapper<Cart>()
                    .eq(Cart::getUserId, userId)
                    .eq(Cart::getGoodsId, it.getGoodsId());
            if (it.getSkuId() != null) {
                q.eq(Cart::getSkuId, it.getSkuId());
            }
            cartMapper.delete(q);
        }

        return order;
    }

    /**
     * 根据用户分页查询订单（简化：返回 list）
     */
    public List<Order> listByUser(Integer userId, Integer page, Integer limit) {
        // 简化：不做复杂分页逻辑，直接按 create_time desc
        int offset = Math.max(0, (page - 1) * limit);
        return orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime)
                        .last("LIMIT " + offset + "," + limit)
        );
    }

    /**
     * 订单详情（含 items）
     */
    public Map<String, Object> getOrderDetail(Integer userId, Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权限");
        }
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        Map<String, Object> res = new HashMap<>();
        res.put("order", order);
        res.put("items", items);
        return res;
    }

    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Integer userId, Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权限");
        }
        if (order.getPaidTime() != null && order.getPaidTime() > 0) {
            throw new IllegalStateException("已支付订单不能取消");
        }
        order.setClosed(true);
        order.setUpdateTime((int) Instant.now().getEpochSecond());
        orderMapper.updateById(order);
    }

    /**
     * 模拟支付
     */
    @Transactional
    public void payOrder(Integer userId, Integer orderId, String paymentMethod, String paymentNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权限");
        }
        if (order.getPaidTime() != null && order.getPaidTime() > 0) {
            throw new IllegalStateException("订单已支付");
        }
        order.setPaymentMethod(paymentMethod);
        order.setPaymentNo(paymentNo);
        order.setPaidTime((int) Instant.now().getEpochSecond());
        order.setUpdateTime((int) Instant.now().getEpochSecond());
        orderMapper.updateById(order);
    }

    // helper: 生成订单号
    private String genOrderNo() {
        return "ORD" + Instant.now().toEpochMilli() + (new Random().nextInt(9000) + 1000);
    }

    // placeholder: 根据 goodsId/skuId 获取单价（必须由你实现）
    private BigDecimal fetchSkuPrice(Integer goodsId, Integer skuId) {
        // TODO: 用你的 goods/sku mapper 查询真实单价
        // 示例：如果没有 sku，读 goods 表 price；若有 sku，则读 sku.price
        // 下面为占位：抛异常以提醒你替换
        throw new UnsupportedOperationException("请实现 fetchSkuPrice(goodsId, skuId) 来从商品/sku 表获取单价");
    }

}

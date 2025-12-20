package com.bik.flower_shop.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bik.flower_shop.enumeration.*;
import com.bik.flower_shop.mapper.*;
import com.bik.flower_shop.pojo.dto.*;
import com.bik.flower_shop.pojo.entity.*;
import com.bik.flower_shop.pojo.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@RequiredArgsConstructor
@Service
public class OrdersUserService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final ObjectMapper objectMapper;
    private final CartMapper cartMapper;
    private final CouponMapper couponMapper;
    private final CouponUserMapper couponUserMapper;
    //    private final GoodsSkuMapper goodsSkuMapper;
    private final UserAddressesMapper userAddressesMapper;
    private final GoodsMapper goodsMapper;


    /**
     * 创建订单，返回 orderId 或订单对象
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Integer userId, OrderCreateDTO dto) throws JsonProcessingException {

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单项不能为空");
        }

        int now = (int) Instant.now().getEpochSecond();

        // 1. 商品小计 & 订单项
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderCreateItemDTO it : dto.getItems()) {
            BigDecimal unitPrice = fetchSkuPrice(it.getGoodsId());
            if (unitPrice == null) {
                throw new IllegalArgumentException("商品不存在：" + it.getGoodsId());
            }

            Goods goods = goodsMapper.selectById(it.getGoodsId());
            if (goods == null) {
                throw new IllegalArgumentException("商品不存在：" + it.getGoodsId());
            }

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(it.getNum()));
            subtotal = subtotal.add(lineTotal);

            OrderItem oi = new OrderItem();
            oi.setGoodsId(it.getGoodsId());
            oi.setNum(it.getNum());
            oi.setPrice(unitPrice);
            oi.setGoodsNum(it.getNum());
            oi.setSkusType((byte) 0);
            oi.setUserId(userId);
            oi.setGoodsTitle(goods.getTitle());
            oi.setGoodsCover(goods.getCover());
            oi.setCreateTime(now);

            orderItems.add(oi);
        }

        // 2. 优惠券处理
        BigDecimal couponDiscount = BigDecimal.ZERO;
        Integer couponUserId = dto.getCouponId();
        CouponUser usedCouponUser = null;

        if (couponUserId != null) {
            usedCouponUser = couponUserMapper.selectById(couponUserId);
            if (usedCouponUser == null || !Objects.equals(usedCouponUser.getUserId(), userId)) {
                throw new IllegalArgumentException("优惠券不可用");
            }
            if (usedCouponUser.getUsed() != null && usedCouponUser.getUsed() == 1) {
                throw new IllegalArgumentException("优惠券已使用");
            }

            Coupon coupon = couponMapper.selectById(usedCouponUser.getCouponId());
            if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
                throw new IllegalArgumentException("优惠券不可用");
            }

            int nowSec = (int) Instant.now().getEpochSecond();
            if (coupon.getStartTime() != null && coupon.getStartTime() > nowSec) {
                throw new IllegalArgumentException("优惠券尚未生效");
            }
            if (coupon.getEndTime() != null && coupon.getEndTime() < nowSec) {
                throw new IllegalArgumentException("优惠券已过期");
            }

            BigDecimal minPrice = coupon.getMinPrice() == null ? BigDecimal.ZERO : coupon.getMinPrice();
            if (subtotal.compareTo(minPrice) < 0) {
                throw new IllegalArgumentException("未达到优惠券使用门槛");
            }

            // 固定金额
            if (coupon.getType() == 0) {
                couponDiscount = coupon.getValue();
            }
            // 折扣（如 8 = 8 折）
            else {
                BigDecimal discountRate = coupon.getValue()
                        .divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);
                couponDiscount = subtotal.multiply(BigDecimal.ONE.subtract(discountRate))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            if (couponDiscount.compareTo(subtotal) > 0) {
                couponDiscount = subtotal;
            }
        }

        //3. 配送方式
        OrderExtraDTO extra = new OrderExtraDTO();
        OrderExtraDTO.ShippingInfo shipping = new OrderExtraDTO.ShippingInfo();

        if ("express".equals(dto.getShippingType())) {
            shipping.setType("express");
            shipping.setName("加急配送");
            shipping.setFee(BigDecimal.valueOf(30));
        } else {
            shipping.setType("standard");
            shipping.setName("普通配送");
            shipping.setFee(BigDecimal.valueOf(10));
        }
        extra.setShipping(shipping);

        BigDecimal shippingFee = shipping.getFee();

        //4. 总价计算
        BigDecimal totalPrice = subtotal
                .add(shippingFee)
                .subtract(couponDiscount);

        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            totalPrice = BigDecimal.ZERO;
        }

        // 5. 地址快照
        if (dto.getAddressId() == null) {
            throw new IllegalArgumentException("地址不能为空");
        }

        UserAddresses addr = userAddressesMapper.selectById(dto.getAddressId());
        if (addr == null || !Objects.equals(addr.getUserId(), userId)) {
            throw new IllegalArgumentException("地址不存在或不属于当前用户");
        }

        OrderAddressDTO snapshot = new OrderAddressDTO();
        snapshot.setId(addr.getId());
        snapshot.setUserId(addr.getUserId());
        snapshot.setProvince(addr.getProvince());
        snapshot.setCity(addr.getCity());
        snapshot.setDistrict(addr.getDistrict());
        snapshot.setAddress(addr.getAddress());
        snapshot.setZip(addr.getZip() == null ? null : String.valueOf(addr.getZip()));
        snapshot.setName(addr.getName());
        snapshot.setPhone(addr.getPhone());

        String addressSnapshot = objectMapper.writeValueAsString(snapshot);

        // 6. 创建订单
        Orders orders = new Orders();
        orders.setNo(genOrderNo());
        orders.setUserId(userId);
        orders.setSubtotal(subtotal);
        orders.setShippingFee(shippingFee);
        orders.setDiscount(couponDiscount);
        orders.setTotalPrice(totalPrice);
        orders.setRemark(dto.getRemark());
        orders.setReviewed(false);
        orders.setAddressSnapshot(addressSnapshot);
        orders.setExtra(objectMapper.writeValueAsString(extra));
        orders.setCreateTime(now);
        orders.setUpdateTime(now);

        orders.setPayStatus(PayStatusEnum.UNPAID.getCode());
        orders.setShipStatus(ShipStatusEnum.PENDING.getCode());
        orders.setClosed(false);

        // 默认无退款
        orders.setRefundStatus(RefundStatusEnum.NONE.getCode());

        orders.setDeletedByAdmin(0);
        orders.setDeletedByUser(0);
        orders.setClosed(false);

        orders.setReviewed(false);

        if (couponUserId != null) {
            orders.setCouponUserId(couponUserId);
        }

        ordersMapper.insert(orders);

        //7. 保存订单商品
        for (OrderItem oi : orderItems) {
            oi.setOrderId(orders.getId());
            orderItemMapper.insert(oi);
        }

        //8. 标记优惠券已使用
        if (usedCouponUser != null) {
            usedCouponUser.setUsed((byte) 1);
            usedCouponUser.setUpdateTime(now);
            couponUserMapper.updateById(usedCouponUser);
        }

        //9. 删除购物车
        for (OrderCreateItemDTO it : dto.getItems()) {
            cartMapper.delete(
                    new LambdaQueryWrapper<Cart>()
                            .eq(Cart::getUserId, userId)
                            .eq(Cart::getGoodsId, it.getGoodsId())
            );
        }

        //10. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orders.getId());
        result.put("orderNo", orders.getNo());
        result.put("subtotal", subtotal);
        result.put("shippingFee", shippingFee);
        result.put("discount", couponDiscount);
        result.put("totalPrice", totalPrice);

        return result;
    }

    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Integer userId, Integer orderId) {
        Orders orders = ordersMapper.selectById(orderId);
        if (orders == null || !orders.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权限");
        }
        if (orders.getPaidTime() != null && orders.getPaidTime() > 0) {
            throw new IllegalStateException("已支付订单不能取消");
        }
        orders.setClosed(true);
        orders.setUpdateTime((int) Instant.now().getEpochSecond());
        ordersMapper.updateById(orders);
    }

    /**
     * 模拟支付
     *
     * @param userId    用户ID
     * @param orderId   订单ID
     * @param methodStr 支付方式 (WECHAT, ALIPAY)
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Integer userId, Integer orderId, String methodStr) {

        PayMethod method = PayMethod.fromString(methodStr);
        if (method == null) {
            throw new IllegalArgumentException("支付方式不支持");
        }

        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }

        PayStatusEnum currentPay = PayStatusEnum.of(order.getPayStatus());

        // 已支付
        if (currentPay == PayStatusEnum.PAID) {
            throw new IllegalStateException("订单已支付");
        }

        // 已关闭（使用 closed 字段判断）
        if (Boolean.TRUE.equals(order.getClosed())) {
            throw new IllegalStateException("订单已关闭，无法支付");
        }

        int now = (int) Instant.now().getEpochSecond();

        // 模拟延迟 1~3 秒
        try {
            Thread.sleep(1000 + (long) (Math.random() * 2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟支付失败 10%
        if (Math.random() < 0.1) {
            throw new RuntimeException("模拟支付失败，请重试");
        }

        // 支付成功，更新订单状态
        String paymentNo = "MOCK_" + method.name() + "_" + System.currentTimeMillis();

        order.setPayStatus(PayStatusEnum.PAID.getCode());
        order.setPaymentMethod(method.name());
        order.setPaymentNo(paymentNo);
        order.setPaidTime(now);
        order.setUpdateTime(now);

        ordersMapper.updateById(order);
    }

    @Transactional
    public void confirmReceive(Integer userId, Integer orderId) {
        Orders order = ordersMapper.selectOne(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getId, orderId)
                        .eq(Orders::getUserId, userId)
                        .eq(Orders::getDeletedByUser, 0)
        );
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new SecurityException("无权限操作该订单");
        }

        ShipStatusEnum curShip = ShipStatusEnum.of(order.getShipStatus());

        // 防重复确认
        if (curShip == ShipStatusEnum.RECEIVED) {
            throw new IllegalStateException("订单已确认收货");
        }

        if (curShip == ShipStatusEnum.PENDING) {
            throw new IllegalStateException("订单未发货，不能确认收货");
        }

        // 更新订单物流状态
        order.setShipStatus(ShipStatusEnum.RECEIVED.getCode());
        order.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        ordersMapper.updateById(order);

        // 统计销量（真正正确的地方）
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);

        for (OrderItem item : items) {
            goodsMapper.increaseSaleCount(
                    item.getGoodsId(),
                    item.getNum()
            );
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitReview(Integer userId, Integer orderId, ReviewSubmitDTO dto) {

        Orders order = ordersMapper.selectOne(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getId, orderId)
                        .eq(Orders::getUserId, userId)
                        .eq(Orders::getDeletedByUser, 0)
        );
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权限操作");
        }

        // 只允许已收货订单评价
        ShipStatusEnum shipStatusEnum = ShipStatusEnum.of(order.getShipStatus());
        if (shipStatusEnum != ShipStatusEnum.RECEIVED && shipStatusEnum != ShipStatusEnum.RECEIVED) {
            throw new IllegalStateException("订单未收货，无法评价");
        }

        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("评价内容为空");
        }

        int now = (int) (System.currentTimeMillis() / 1000);
        boolean anonymous = Boolean.TRUE.equals(dto.getAnonymous());

        for (ReviewItemDTO itemDto : dto.getItems()) {

            // 校验参数
            if (itemDto.getOrderItemId() == null) {
                throw new IllegalArgumentException("orderItemId 不能为空");
            }
            if (itemDto.getRating() == null || itemDto.getRating() < 1 || itemDto.getRating() > 5) {
                throw new IllegalArgumentException("评分必须在 1~5 之间");
            }

            // 查询订单商品
            OrderItem oi = orderItemMapper.selectById(itemDto.getOrderItemId());
            if (oi == null) {
                throw new IllegalArgumentException("订单商品不存在: " + itemDto.getOrderItemId());
            }

            // 安全校验
            if (!oi.getOrderId().equals(orderId)) {
                throw new SecurityException("订单商品不属于该订单");
            }

            // 防止重复评价
            if (oi.getReviewTime() != null) {
                throw new IllegalStateException("该商品已评价，不能重复提交");
            }

            // 写入评价数据
            oi.setRating(itemDto.getRating());
            oi.setReview(itemDto.getContent());
            oi.setAnonymous(anonymous);
            oi.setReviewImages(
                    itemDto.getImages() == null ? null : JSON.toJSONString(itemDto.getImages())
            );
            oi.setReviewTime(now);
            oi.setReviewStatusEnum(ReviewStatusEnum.REVIEWED);

            orderItemMapper.updateById(oi);

            Goods goods = goodsMapper.selectById(oi.getGoodsId());
            BigDecimal oldRating = goods.getRating() == null ? BigDecimal.ZERO : goods.getRating();
            int oldCount = goods.getReviewCount() == null ? 0 : goods.getReviewCount();

            BigDecimal newRating = oldRating.multiply(BigDecimal.valueOf(oldCount))
                    .add(BigDecimal.valueOf(itemDto.getRating()))
                    .divide(BigDecimal.valueOf(oldCount + 1), 2, RoundingMode.HALF_UP);

            goods.setRating(newRating);
            goods.setReviewCount(oldCount + 1);
            goodsMapper.updateById(goods);

        }

        // 判断是否整单已评价
        int totalItems = orderItemMapper.countByOrderId(orderId);
        int reviewedItems = orderItemMapper.countReviewedByOrderId(orderId);
        if (totalItems > 0 && reviewedItems >= totalItems) {
            order.setReviewed(true);
            order.setUpdateTime(now);
            ordersMapper.updateById(order);
        }
    }

    public List<ReviewItemVO> getReviewItems(Integer userId, Integer orderId) {
        Orders order = ordersMapper.selectOne(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getId, orderId)
                        .eq(Orders::getUserId, userId)
                        .eq(Orders::getDeletedByUser, 0)
        );
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权限查看");
        }

        // 只有已确认收货的订单可以评价
        ShipStatusEnum shipStatusEnum = ShipStatusEnum.of(order.getShipStatus());
        if (shipStatusEnum != ShipStatusEnum.RECEIVED) {
            return Collections.emptyList();
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        List<ReviewItemVO> vos = new ArrayList<>();
        for (OrderItem it : items) {
            ReviewItemVO vo = new ReviewItemVO();
            vo.setOrderItemId(it.getId());
            vo.setGoodsId(it.getGoodsId());
            vo.setGoodsTitle(it.getGoodsTitle());
            vo.setGoodsCover(it.getGoodsCover());
            vo.setNum(it.getNum() != null ? it.getNum() : (it.getGoodsNum() != null ? it.getGoodsNum() : 0));
            // canReview 如果 review_time 未设置且 rating 为空
            boolean canReview = (it.getReviewTime() == null || it.getRating() == null);
            vo.setCanReview(canReview);
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 根据用户分页查询订单
     */
    public OrderListResponse listOrdersByUser(Integer userId, Integer page, Integer limit) throws JsonProcessingException {
        int offset = Math.max(0, (page - 1) * limit);

        // 查询总数
        long totalCount = ordersMapper.selectCount(
                new LambdaQueryWrapper<Orders>().
                        eq(Orders::getUserId, userId)
                        .eq(Orders::getDeletedByUser, 0)
        );

        // 查询分页订单列表
        List<Orders> orders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getUserId, userId)
                        .eq(Orders::getDeletedByUser, 0)
                        .orderByDesc(Orders::getCreateTime)
                        .last("LIMIT " + offset + "," + limit)
        );

        List<OrderUserListVO> list = new ArrayList<>();
        if (!orders.isEmpty()) {
            List<Integer> orderIds = orders.stream().map(Orders::getId).toList();
            List<OrderItem> allItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds)
            );
            Map<Integer, List<OrderItem>> itemsMap = allItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderId));

            ObjectMapper mapper = new ObjectMapper();

            for (Orders o : orders) {
                OrderUserListVO vo = new OrderUserListVO();
                vo.setOrderId(o.getId());
                vo.setOrderNo(o.getNo());
                vo.setTotalPrice(o.getTotalPrice());
                vo.setShipStatus(o.getShipStatus());
                vo.setCreateTime(o.getCreateTime());
                vo.setSubtotal(o.getSubtotal());
                vo.setShippingFee(o.getShippingFee());
                vo.setDiscount(o.getDiscount());
                vo.setExpireTime(calcExpireTime(o));
                vo.setPayStatus(o.getPayStatus());
                vo.setReviewed(o.getReviewed());
                // 地址反序列化
                if (o.getAddressSnapshot() != null) {
                    vo.setAddress(mapper.readValue(o.getAddressSnapshot(), OrderAddressDTO.class));
                }

                // 商品列表
                List<OrderItem> items = itemsMap.getOrDefault(o.getId(), Collections.emptyList());
                vo.setItems(convertItems(items));

                list.add(vo);
            }
        }

        // 封装返回
        OrderListResponse response = new OrderListResponse();
        response.setList(list);
        response.setTotalCount(totalCount);

        return response;
    }

    /**
     * 订单详情
     */
    public OrderDetailVO getOrderDetail(Integer userId, Integer orderId) throws JsonProcessingException {
        Orders o = ordersMapper.selectOne(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getId, orderId)
                        .eq(Orders::getUserId, userId)
                        .eq(Orders::getDeletedByUser, 0)
        );
        if (o == null || !o.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在");
        }

        OrderDetailVO vo = new OrderDetailVO();

        vo.setOrderId(o.getId());
        vo.setOrderNo(o.getNo());
        vo.setTotalPrice(o.getTotalPrice());
        vo.setShipStatus(o.getShipStatus());
        vo.setCreateTime(o.getCreateTime());
        vo.setSubtotal(o.getSubtotal());
        vo.setShipping(o.getShippingFee());
        vo.setDiscount(o.getDiscount());
        vo.setExpireTime(calcExpireTime(o));
        vo.setPayStatus(o.getPayStatus());
        // 地址
        vo.setAddress(
                objectMapper.readValue(o.getAddressSnapshot(), OrderAddressDTO.class)
        );

        // 商品
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, o.getId())
        );
        vo.setItems(convertItems(items));

        return vo;
    }

    private List<OrderUserItemVO> convertItems(List<OrderItem> items) {

        List<OrderUserItemVO> list = new ArrayList<>(items.size());

        for (OrderItem oi : items) {
            OrderUserItemVO vo = new OrderUserItemVO();

            vo.setGoodsId(oi.getGoodsId());
            // 建议下单时存快照
            vo.setGoodsTitle(oi.getGoodsTitle());
            // 建议下单时存快照
            vo.setGoodsCover(oi.getGoodsCover());
            vo.setPrice(oi.getPrice());
            vo.setNum(oi.getNum());

            vo.setSubtotal(
                    oi.getPrice().multiply(BigDecimal.valueOf(oi.getNum()))
            );

            list.add(vo);
        }

        return list;
    }

    // helper: 生成订单号
    private String genOrderNo() {
        return "ORD" + Instant.now().toEpochMilli() + (new Random().nextInt(9000) + 1000);
    }

    private BigDecimal fetchSkuPrice(Integer goodsId) {
        if (goodsId == null) {
            return null;
        }
        Goods g = goodsMapper.selectById(goodsId);
        if (g == null) {
            return null;
        }
        return g.getMinPrice();
    }

    /**
     * 计算订单剩余秒数（倒计时），只针对未支付且未关闭订单
     *
     * @param o 订单实体
     * @return 剩余秒数，如果已过期或不需倒计时返回 0，否则返回剩余秒数
     */
    private Integer calcExpireTime(Orders o) {
        // 仅未支付且未关闭订单才有倒计时
        if (!PayStatusEnum.UNPAID.getCode().equals(o.getPayStatus()) || Boolean.TRUE.equals(o.getClosed())) {
            return 0;
        }

        int now = (int) Instant.now().getEpochSecond();
        // 订单过期时间戳
        int expireTime = o.getCreateTime() + 30 * 60;
        int remaining = expireTime - now;

        return Math.max(remaining, 0);
    }

    /**
     * 用户软删除订单
     */
    @Transactional
    public int markDeletedByUser(Integer userId, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return ordersMapper.markDeletedByUser(userId, ids);
    }

    public Orders getOrderById(Long orderId) {
        return ordersMapper.selectById(orderId);
    }
}

package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.enumeration.PayStatusEnum;
import com.bik.flower_shop.enumeration.RefundStatusEnum;
import com.bik.flower_shop.enumeration.ShipStatusEnum;
import com.bik.flower_shop.mapper.*;
import com.bik.flower_shop.pojo.dto.OrderAddressDTO;
import com.bik.flower_shop.pojo.dto.OrderListQueryDTO;
import com.bik.flower_shop.pojo.dto.ShipOrderRequest;
import com.bik.flower_shop.pojo.entity.ExpressCompany;
import com.bik.flower_shop.pojo.entity.Goods;
import com.bik.flower_shop.pojo.entity.OrderItem;
import com.bik.flower_shop.pojo.entity.Orders;
import com.bik.flower_shop.pojo.vo.*;
import com.bik.flower_shop.utils.JsonExtraUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrdersAdminService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final ExpressCompanyMapper expressCompanyMapper;
    private final OrderShipService orderShipService;


    @Resource
    private JsonExtraUtil jsonExtraUtil;

    @Transactional
    public int deleteOrdersBulk(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        int updated = ordersMapper.markDeletedByAdmin(ids);
        return updated;
    }


    public OrderAdminPageVO listOrders(OrderListQueryDTO dto) throws JsonProcessingException {

        int pageNo = Math.max(1, dto.getPage() == null ? 1 : dto.getPage());
        int limit = dto.getLimit() == null ? 10 : dto.getLimit();

        Page<Orders> page = new Page<>(pageNo, limit);

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getDeletedByAdmin, 0);
        // 订单号过滤（精确）
        if (dto.getNo() != null && !dto.getNo().isBlank()) {
            wrapper.eq(Orders::getNo, dto.getNo());
        }

        // tab 处理
        String tab = dto.getTab() == null ? "all" : dto.getTab();

        switch (tab) {

            case "nopay":
                // 待支付
                wrapper.eq(Orders::getPayStatus, PayStatusEnum.UNPAID.getCode())
                        .eq(Orders::getClosed, false);
                break;

            case "noship":
                // 待发货（已支付、未发货、未进入退款流程）
                wrapper.eq(Orders::getPayStatus, PayStatusEnum.PAID.getCode())
                        .eq(Orders::getShipStatus, ShipStatusEnum.PENDING.getCode())
                        .in(Orders::getRefundStatus, RefundStatusEnum.NONE.getCode(), RefundStatusEnum.COMPLETED.getCode());
                break;

            case "shipped":
                // 待收货
                wrapper.eq(Orders::getShipStatus, ShipStatusEnum.SHIPPED.getCode())
                        .in(Orders::getRefundStatus, RefundStatusEnum.NONE.getCode(), RefundStatusEnum.COMPLETED.getCode());
                break;

            case "received":
                // 已收货
                wrapper.eq(Orders::getShipStatus, ShipStatusEnum.RECEIVED.getCode())
                        .in(Orders::getRefundStatus, RefundStatusEnum.NONE.getCode(), RefundStatusEnum.COMPLETED.getCode());
                break;

            case "refunding":
                // 退款中（整个退款流程，包括用户已申请退货）
                wrapper.in(
                        Orders::getRefundStatus,
                        RefundStatusEnum.PENDING.getCode(),
                        RefundStatusEnum.AGREED.getCode(),
                        RefundStatusEnum.RETURN_REQUESTED.getCode(),
                        RefundStatusEnum.RETURNING.getCode()
                );
                break;

            case "closed":
                // 已关闭
                wrapper.eq(Orders::getClosed, true);
                break;

            case "all":
            default:
                // 全部，不加条件
                break;
        }


        wrapper.orderByDesc(Orders::getCreateTime);

        // 执行分页查询
        Page<Orders> resultPage = ordersMapper.selectPage(page, wrapper);
        List<Orders> orders = resultPage.getRecords();

        OrderAdminPageVO pageVO = new OrderAdminPageVO();
        if (orders.isEmpty()) {
            pageVO.setList(Collections.emptyList());
            pageVO.setTotalCount(resultPage.getTotal());
            return pageVO;
        }


        List<Integer> orderIds = orders.stream()
                .map(Orders::getId)
                .toList();


        // 批量拉取 order item 并按 orderId 分组，避免 N+1
        List<OrderItem> allItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .in(OrderItem::getOrderId, orderIds)
        );

        Map<Integer, List<OrderItem>> itemsMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        Set<Integer> goodsIds = allItems.stream()
                .map(OrderItem::getGoodsId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, Goods> goodsMap = goodsIds.isEmpty()
                ? Collections.emptyMap()
                : goodsMapper.selectList(
                new LambdaQueryWrapper<Goods>()
                        .in(Goods::getId, goodsIds)
                        .select(Goods::getId, Goods::getTitle, Goods::getCover)
        ).stream().collect(Collectors.toMap(Goods::getId, g -> g));

        ObjectMapper mapper = new ObjectMapper();

        List<OrderAdminListVO> voList = new ArrayList<>();

        for (Orders o : orders) {

            OrderAdminListVO vo = new OrderAdminListVO();
            vo.setId(o.getId());
            vo.setOrderNo(o.getNo());
            vo.setTotalPrice(o.getTotalPrice());
            vo.setSubtotal(o.getSubtotal());
            vo.setShippingFee(o.getShippingFee());
            vo.setDiscount(o.getDiscount());
            vo.setPayStatus(o.getPayStatus());
            vo.setShipStatus(o.getShipStatus());
            vo.setRefundStatus(o.getRefundStatus());
            vo.setReviewed(o.getReviewed());
            vo.setClosed(o.getClosed());
            vo.setCreateTime(o.getCreateTime());
            vo.setPaymentMethod(o.getPaymentMethod());
            vo.setPaidTime(o.getPaidTime());
            vo.setPaymentNo(o.getPaymentNo());
            // 处理 extra
            if (o.getExtra() != null && !o.getExtra().isEmpty()) {
                Map<String, Object> extraMap = mapper.readValue(o.getExtra(), Map.class);
                vo.setExtra(extraMap);
            }

            // 处理 shipData
            if (StringUtils.hasText(o.getShipData())) {
                Map<String, Object> shipDataMap = mapper.readValue(o.getShipData(), Map.class);

                // 只取部分信息
                Map<String, Object> briefShipData = new HashMap<>();
                briefShipData.put("company", shipDataMap.get("company"));
                briefShipData.put("trackingNo", shipDataMap.get("trackingNo"));
                briefShipData.put("shippedTime", shipDataMap.get("shippedTime"));

                vo.setShipData(briefShipData);
            }


            // 用户
            vo.setUser(userMapper.selectSimpleById(o.getUserId()));

            // 地址
            if (StringUtils.hasText(o.getAddressSnapshot())) {
                vo.setAddress(mapper.readValue(o.getAddressSnapshot(), OrderAddressDTO.class));
            }

            // 商品
            List<OrderAdminItemVO> itemVOs = itemsMap
                    .getOrDefault(o.getId(), Collections.emptyList())
                    .stream()
                    .map(it -> {
                        OrderAdminItemVO itemVO = new OrderAdminItemVO();
                        itemVO.setGoodsId(it.getGoodsId());
                        itemVO.setPrice(it.getPrice());
                        itemVO.setNum(it.getNum());
                        itemVO.setSkusType(it.getSkusType());


                        Goods g = goodsMap.get(it.getGoodsId());
                        if (g != null) {
                            itemVO.setGoodsTitle(g.getTitle());
                            itemVO.setGoodsCover(g.getCover());
                        }
                        return itemVO;
                    }).toList();

            vo.setItems(itemVOs);

            // 收货人 / 手机过滤
            if (dto.getName() != null || dto.getPhone() != null) {
                OrderAddressDTO addr = vo.getAddress();
                if (addr == null) {
                    continue;
                }
                if (StringUtils.hasText(dto.getName()) && !addr.getName().contains(dto.getName())) {
                    continue;
                }
                if (StringUtils.hasText(dto.getPhone()) && !addr.getPhone().contains(dto.getPhone())) {
                    continue;
                }
            }

            voList.add(vo);
        }

        pageVO.setList(voList);
        pageVO.setTotalCount(resultPage.getTotal());
        return pageVO;
    }


    @Transactional
    public void shipOrder(Long id, ShipOrderRequest req) throws JsonProcessingException {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + id);
        }

        // 统一通过枚举解析当前状态（of 方法需在 enum 中实现）
        PayStatusEnum payStatus = PayStatusEnum.of(order.getPayStatus());
        ShipStatusEnum shipStatus = ShipStatusEnum.of(order.getShipStatus());
        RefundStatusEnum refundStatus = RefundStatusEnum.of(order.getRefundStatus());

        // 已关闭订单不能发货
        if (Boolean.TRUE.equals(order.getClosed())) {
            throw new IllegalStateException("订单已关闭，不能发货");
        }

        // 必须已支付
        if (payStatus != PayStatusEnum.PAID) {
            throw new IllegalStateException("订单未支付，不能发货");
        }

        // 已发货 / 已收货 不可重复发货
        if (shipStatus == ShipStatusEnum.SHIPPED || shipStatus == ShipStatusEnum.RECEIVED) {
            throw new IllegalStateException("订单已发货，不可重复发货");
        }

        // 退款中 / 已同意退款，不允许发货
        if (refundStatus.isProcessing()) {
            throw new IllegalStateException("订单退款处理中，不能发货");
        }

        // 查公司
        ExpressCompany company = null;
        if (req.getExpressCompanyId() != null) {
            company = expressCompanyMapper.selectById(req.getExpressCompanyId());
        }

        String companyCode = company != null ? company.getCode() : null;

        // 生成物流数据（内部可能写入 shipData），然后写回发货状态和更新时间以保证一致性
        orderShipService.generateMockShipData(
                order.getId(),
                companyCode,
                req.getExpressNo()
        );

        // write-back: 标记已发货并更新更新时间（防止 generateMockShipData 未修改 shipStatus）
        order.setShipStatus(ShipStatusEnum.SHIPPED.getCode());
        order.setUpdateTime((int) (System.currentTimeMillis() / 1000L));
        ordersMapper.updateById(order);
    }


    @Transactional
    public void handleRefund(Integer orderId, boolean agree, String adminReason, String refundType) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        // 1. 校验支付状态
        if (PayStatusEnum.of(order.getPayStatus()) != PayStatusEnum.PAID) {
            throw new IllegalStateException("未支付订单不能退款");
        }

        // 2. 当前退款状态
        RefundStatusEnum cur = RefundStatusEnum.of(
                order.getRefundStatus() == null ? RefundStatusEnum.NONE.getCode() : order.getRefundStatus()
        );

        // 3. 校验 refundType 和对应状态
        boolean isOnlyRefund = "only_refund".equalsIgnoreCase(refundType);
        boolean isReturnRefund = "return_refund".equalsIgnoreCase(refundType);

        if (!isOnlyRefund && !isReturnRefund) {
            throw new IllegalArgumentException("非法的退款类型");
        }

        if (isOnlyRefund && cur != RefundStatusEnum.PENDING) {
            throw new IllegalStateException("当前订单没有待处理的退款申请");
        }

        if (isReturnRefund && cur != RefundStatusEnum.RETURN_REQUESTED) {
            throw new IllegalStateException("当前订单没有待处理的退货申请");
        }

        long now = System.currentTimeMillis() / 1000;

        // 4. 构造退款日志
        Map<String, Object> refundLog = new HashMap<>();
        refundLog.put("adminTime", now);
        refundLog.put("adminReason", adminReason);
        refundLog.put("refundType", refundType);

        if (agree) {
            refundLog.put("adminAction", "agreed");

            if (isOnlyRefund) {
                // 仅退款：用户未发货
                if (!"pending".equalsIgnoreCase(order.getShipStatus())) {
                    throw new IllegalStateException("已发货订单不能仅退款");
                }
                // 回滚库存
                List<OrderItem> items = orderItemMapper.selectList(
                        new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
                );
                for (OrderItem item : items) {
                    goodsMapper.increaseStock(item.getGoodsId(), item.getNum());
                }
                order.setRefundStatus(RefundStatusEnum.COMPLETED.getCode());
                order.setPayStatus(PayStatusEnum.REFUNDED.getCode());
                order.setRefundNo("REF" + System.currentTimeMillis());
                order.setClosed(true);

            } else if (isReturnRefund) {
                // 退货退款：用户已收货
                if (!"received".equalsIgnoreCase(order.getShipStatus())) {
                    throw new IllegalStateException("未收货订单不能退货退款");
                }
                // 状态改为等待用户退货
                order.setRefundStatus(RefundStatusEnum.AGREED.getCode());
            }

        } else {
            // 拒绝退款
            order.setRefundStatus(RefundStatusEnum.REJECTED.getCode());
            refundLog.put("adminAction", "rejected");
        }

        // 5. 写入 extra
        order.setExtra(jsonExtraUtil.put(order.getExtra(), "refund_result", refundLog));
        order.setUpdateTime((int) now);
        ordersMapper.updateById(order);
    }


    @Transactional
    public void confirmRefund(Integer orderId) {
        int now = (int) (System.currentTimeMillis() / 1000);

        // 原子更新订单状态（防并发）
        int rows = ordersMapper.update(
                null,
                new LambdaUpdateWrapper<Orders>()
                        .eq(Orders::getId, orderId)
                        .eq(Orders::getRefundStatus, RefundStatusEnum.RETURNING.getCode())
                        .set(Orders::getRefundStatus, RefundStatusEnum.COMPLETED.getCode())
                        .set(Orders::getPayStatus, PayStatusEnum.REFUNDED.getCode())
                        .set(Orders::getClosed, true)
                        .set(Orders::getUpdateTime, now)
        );

        if (rows == 0) {
            throw new IllegalStateException("订单状态异常或已处理退款");
        }

        // 查询订单项
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
        );

        // 回滚库存（只会执行一次）
        for (OrderItem item : items) {
            int r = goodsMapper.increaseStock(item.getGoodsId(), item.getNum());
            if (r == 0) {
                throw new IllegalStateException("库存回滚失败");
            }
        }

        // 记录日志
        try {
            Orders order = ordersMapper.selectById(orderId);

            Map<String, Object> log = new HashMap<>();
            log.put("confirmTime", now);

            String extra = order.getExtra();
            if (extra == null) {
                extra = "{}";
            }

            order.setExtra(
                    jsonExtraUtil.put(extra, "refund_confirm", log)
            );

            ordersMapper.updateById(order);
        } catch (Exception e) {
            log.error("退款确认日志写入失败，orderId={}", orderId, e);
        }
    }

    public void exportOrders() {
        // 导出占位：生产环境用 Apache POI 生成 Excel 并通过 response 输出
        // 这里不实现具体导出逻辑，只作占位
    }

    public Orders getOrderById(Long orderId) {
        return ordersMapper.selectById(orderId);
    }


    @Transactional
    public void rejectReturn(Integer orderId, String reason) {
        int now = (int) (System.currentTimeMillis() / 1000);

        // 原子更新订单状态：只允许拒绝用户申请退货
        int rows = ordersMapper.update(
                null,
                new LambdaUpdateWrapper<Orders>()
                        .eq(Orders::getId, orderId)
                        .eq(Orders::getRefundStatus, RefundStatusEnum.RETURNING.getCode()) // 允许拒绝已寄回订单
                        .set(Orders::getRefundStatus, RefundStatusEnum.REJECTED.getCode())
                        .set(Orders::getUpdateTime, now)
        );


        if (rows == 0) {
            throw new IllegalStateException("订单状态异常或不允许拒绝退货");
        }

        // 查询订单
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        // 写入拒绝日志
        Map<String, Object> logger = new HashMap<>();
        logger.put("rejectTime", now);
        logger.put("reason", reason);

        try {
            String extra = order.getExtra();
            if (extra == null) {
                extra = "{}";
            }
            order.setExtra(jsonExtraUtil.put(extra, "return_reject", logger));
            ordersMapper.updateById(order);
        } catch (Exception e) {
            log.error("拒绝退货日志写入失败，orderId={}", orderId, e);
        }
    }

}

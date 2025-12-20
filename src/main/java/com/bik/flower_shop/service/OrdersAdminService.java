package com.bik.flower_shop.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@RequiredArgsConstructor
@Service
public class OrdersAdminService {

    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final ExpressCompanyMapper expressCompanyMapper;
    private final OrderShipService orderShipService;


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
                wrapper.eq(Orders::getPayStatus, PayStatusEnum.UNPAID.getCode())
                        .eq(Orders::getClosed, false);
                break;
            case "noship":
                wrapper.eq(Orders::getPayStatus, PayStatusEnum.PAID.getCode())
                        .eq(Orders::getShipStatus, ShipStatusEnum.PENDING.getCode());
                break;
            case "shipped":
                wrapper.eq(Orders::getShipStatus, ShipStatusEnum.SHIPPED.getCode());
                break;
            case "received":
                wrapper.eq(Orders::getShipStatus, ShipStatusEnum.RECEIVED.getCode());
                break;
            case "refunding":
                wrapper.eq(Orders::getRefundStatus, RefundStatusEnum.PENDING.getCode());
                break;
            case "closed":
                wrapper.eq(Orders::getClosed, true);
                break;
            default:
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
    public void handleRefund(Long id, boolean agree, String reason) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在:" + id);
        }

        // 使用 refundStatus + payStatus 判定，不使用 orderStatus
        RefundStatusEnum refundStatus = RefundStatusEnum.of(order.getRefundStatus());
        PayStatusEnum payStatus = PayStatusEnum.of(order.getPayStatus());

        // 只有已支付订单才能进入退款处理（按你业务可调整）
        if (payStatus != PayStatusEnum.PAID) {
            throw new IllegalStateException("未支付订单不可退款处理");
        }

        // 必须处于退款中（pending）才允许处理
        if (refundStatus != RefundStatusEnum.PENDING) {
            throw new IllegalStateException("订单当前状态不可退款处理");
        }

        if (agree) {
            order.setRefundStatus(RefundStatusEnum.AGREED.getCode());
            // 注意：实际退款成功后，应该在退款回调里把 payStatus 设置为 REFUNDED，并根据业务设置 closed 等
        } else {
            order.setRefundStatus(RefundStatusEnum.REJECTED.getCode());
            // 将拒绝理由写入 extra
            Map<String, Object> extra = new HashMap<>();
            extra.put("refund_reject_reason", reason);
            order.setExtra(JSON.toJSONString(extra));
        }
        order.setUpdateTime((int) (System.currentTimeMillis() / 1000L));
        ordersMapper.updateById(order);
    }


    public void exportOrders() {
        // 导出占位：生产环境用 Apache POI 生成 Excel 并通过 response 输出
        // 这里不实现具体导出逻辑，只作占位
    }

    public Orders getOrderById(Long orderId) {
        return ordersMapper.selectById(orderId);
    }
}

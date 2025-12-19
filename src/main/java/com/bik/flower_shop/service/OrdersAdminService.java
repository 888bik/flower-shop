package com.bik.flower_shop.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
        // 这里做物理删除，也可以做软删除（update delete_time）
        int deleted = ordersMapper.deleteBatchIds(ids);
        return deleted;
    }


    public OrderAdminPageVO listOrders(OrderListQueryDTO dto) throws JsonProcessingException {

        int pageNo = Math.max(1, dto.getPage() == null ? 1 : dto.getPage());
        int limit = dto.getLimit() == null ? 10 : dto.getLimit();

        Page<Orders> page = new Page<>(pageNo, limit);

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        // 订单号过滤（精确）
        if (dto.getNo() != null && !dto.getNo().isBlank()) {
            wrapper.eq(Orders::getNo, dto.getNo());
        }

        // tab 处理
        String tab = dto.getTab() == null ? "all" : dto.getTab();
        switch (tab) {
            case "nopay":
                wrapper.eq(Orders::getPayStatus, "pending").eq(Orders::getClosed, false);
                break;
            case "noship":
                wrapper.eq(Orders::getPayStatus, "paid").eq(Orders::getShipStatus, "pending");
                break;
            case "shiped":
                wrapper.eq(Orders::getShipStatus, "shipped");
                break;
            case "received":
                wrapper.eq(Orders::getShipStatus, "received");
                break;
            case "finish":
                wrapper.eq(Orders::getPayStatus, "paid").eq(Orders::getShipStatus, "received");
                break;
            case "closed":
                wrapper.eq(Orders::getClosed, true);
                break;
            case "refunding":
                wrapper.eq(Orders::getRefundStatus, "pending");
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
            vo.setOrderId(o.getId());
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

        // 校验：已支付 && 未发货
        String payStatus = order.getPayStatus();
        String shipStatus = order.getShipStatus();

        if (!"paid".equalsIgnoreCase(payStatus)) {
            throw new IllegalStateException("订单未支付，不能发货");
        }
        if ("shipped".equalsIgnoreCase(shipStatus)) {
            throw new IllegalStateException("订单已发货，不可重复发货");
        }

        // 查公司
        ExpressCompany company = null;
        if (req.getExpressCompanyId() != null) {
            company = expressCompanyMapper.selectById(req.getExpressCompanyId());
        }

        String companyCode = company != null ? company.getCode() : null;

        orderShipService.generateMockShipData(
                order.getId(),
                companyCode,
                req.getExpressNo()
        );
    }


    @Transactional
    public void handleRefund(Long id, boolean agree, String reason) {
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在:" + id);
        }

        if (agree) {
            order.setRefundStatus("agreed");
            // 发起退款流程（第三方 / 内部账变）—— 此处只标记
        } else {
            order.setRefundStatus("rejected");
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

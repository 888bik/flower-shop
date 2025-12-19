package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bik.flower_shop.enumeration.ShipStatusEnum;
import com.bik.flower_shop.mapper.ExpressCompanyMapper;
import com.bik.flower_shop.mapper.OrdersMapper;
import com.bik.flower_shop.pojo.dto.OrderAddressDTO;
import com.bik.flower_shop.pojo.dto.ShipData;
import com.bik.flower_shop.pojo.dto.TrackEvent;
import com.bik.flower_shop.pojo.entity.ExpressCompany;
import com.bik.flower_shop.pojo.entity.Orders;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * OrderShipService - 将 mock 的物流轨迹写入 orders.ship_data（并更新 ship_status）
 *
 * @author bik
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderShipService {

    private final OrdersMapper ordersMapper;
    private final ExpressCompanyMapper expressCompanyMapper;
    private final ObjectMapper objectMapper;
    ;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.ofHours(8);

    @Transactional(rollbackFor = Exception.class)
    public ShipData generateMockShipData(Integer orderId, String companyCode, String trackingNo) throws JsonProcessingException {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + orderId);
        }

        // 校验：已支付 && 未发货
        if (!"paid".equalsIgnoreCase(order.getPayStatus())) {
            throw new IllegalStateException("订单未支付，不能发货: " + orderId);
        }
        if ("shipped".equalsIgnoreCase(order.getShipStatus())) {
            throw new IllegalStateException("订单已发货: " + orderId);
        }

        // 获取快递公司信息（可为空）
        ExpressCompany company = null;
        if (StringUtils.hasText(companyCode)) {
            company = expressCompanyMapper.selectOne(new LambdaQueryWrapper<ExpressCompany>().eq(ExpressCompany::getCode, companyCode));
        }
        if (company == null && StringUtils.hasText(companyCode)) {
            company = new ExpressCompany();
            company.setId(0);
            company.setCode(companyCode);
            company.setName(companyCode);
        }

        // 生成运单号
        if (!StringUtils.hasText(trackingNo)) {
            trackingNo = generateTrackingNoByCompany(company != null ? company.getCode() : null);
        }

        // 准备 ShipData 基础信息
        ShipData ship = new ShipData();
        Map<String, Object> comp = new HashMap<>();
        if (company != null) {
            comp.put("id", company.getId());
            comp.put("name", company.getName());
            comp.put("code", company.getCode());
        } else {
            comp.put("id", 0);
            comp.put("name", companyCode == null ? "unknown" : companyCode);
            comp.put("code", companyCode == null ? "unknown" : companyCode);
        }
        ship.setCompany(comp);
        ship.setTrackingNo(trackingNo);

        // 尝试解析 order.extra 中的 shipping（若存在）
        Map<String, Object> shippingMap = new HashMap<>();
        try {
            if (StringUtils.hasText(order.getExtra())) {
                Map<?, ?> extra = objectMapper.readValue(order.getExtra(), Map.class);
                Object s = extra.get("shipping");
                if (s instanceof Map) {
                    shippingMap.putAll((Map) s);
                }
            }
        } catch (Exception ignored) {
        }
        if (!shippingMap.containsKey("type")) {
            shippingMap.put("type", "standard");
            shippingMap.put("name", "普通配送");
            shippingMap.put("fee", order.getShippingFee() == null ? 0 : order.getShippingFee());
        }
        ship.setShipping(shippingMap);

        // 解析地址，获取目的地省/市/区/详细地址
        String destProvince = null, destCity = null, destDistrict = null, destAddrText = null;
        try {
            if (StringUtils.hasText(order.getAddressSnapshot())) {
                OrderAddressDTO addr = objectMapper.readValue(order.getAddressSnapshot(), OrderAddressDTO.class);
                destProvince = addr.getProvince();
                destCity = addr.getCity();
                destDistrict = addr.getDistrict();
                destAddrText = (addr.getProvince() == null ? "" : addr.getProvince())
                        + (addr.getCity() == null ? "" : " " + addr.getCity())
                        + (addr.getDistrict() == null ? "" : " " + addr.getDistrict())
                        + (addr.getAddress() == null ? "" : " " + addr.getAddress());
            }
        } catch (Exception ignored) {
        }

        // 生成“地域感强”的路线节点
        List<TrackEvent> history = buildRegionalHistory(order.getCreateTime(), destProvince, destCity, destAddrText, company != null ? company.getName() : null);
        ship.setHistory(history);

        // 保存发货时间（揽收时间）
        if (!history.isEmpty()) {
            TrackEvent firstPicked = history.get(history.size() - 1);
            ship.setShippedTime(LocalDateTime.parse(firstPicked.getTime(), TIME_FMT).toEpochSecond(ZONE_OFFSET));
        } else {
            ship.setShippedTime(Instant.now().getEpochSecond());
        }

        // 写回 DB（ship_data 存 JSON 字符串），并更新状态为 shipped
        String json = objectMapper.writeValueAsString(ship);
        order.setShipData(json);
        order.setShipStatus(ShipStatusEnum.SHIPPED.getCode());
        order.setUpdateTime((int) Instant.now().getEpochSecond());

        int updated = ordersMapper.updateById(order);
        if (updated <= 0) {
            throw new IllegalStateException("更新订单物流信息失败: " + orderId);
        }

        log.info("订单{} 写入 mock ship_data company={} trackingNo={}", orderId, comp.get("name"), trackingNo);
        return ship;
    }

    /**
     * 按目的地省份构造更“地域化”的枢纽链路并生成时间点与事件。
     * 返回的 history 【最新在前】（第一条是最近事件）。
     */
    private List<TrackEvent> buildRegionalHistory(Integer orderCreateEpochSeconds, String destProvince, String destCity, String destAddrText, String companyName) {
        // 构建枢纽链（从发货仓到目的地派送站）
        List<String> hubs = buildHubsChain(destProvince, destCity);

        // 基准时间：如果订单创建时间存在，基于它向后分配时间；否则使用 now-2d 作为起点
        LocalDateTime base = (orderCreateEpochSeconds != null && orderCreateEpochSeconds > 0)
                ? LocalDateTime.ofEpochSecond(orderCreateEpochSeconds, 0, ZONE_OFFSET)
                : LocalDateTime.now().minusDays(2);

        // 节点总数（包括揽收 + 每个 hub 的到达/离开 + 派送 + 签收）
        // 我们简化模型：PICKED -> for each hub: DEPARTED(from prev), ARRIVED(at hub) -> 到达目的地 -> OUT_FOR_DELIVERY -> IN_STORAGE(optional) -> SIGNED
        int hubsCount = Math.max(1, hubs.size());
        // 估算总小时: 根据 hubsCount 增加
        long totalHours = 6 + hubsCount * 12L;
        LocalDateTime tCurrent = base.plusHours(2);

        List<EventNode> nodes = new ArrayList<>();
        // 揽收节点
        nodes.add(new EventNode("PICKED", tCurrent, hubs.get(0), "已揽收，快件由发货仓出库"));

        // 对于每个中间枢纽：离开上一节点 -> 到达当前枢纽
        for (String hub : hubs) {
            // depart (上一节点到达后的一个间隔)
            tCurrent = tCurrent.plusHours(6);
            nodes.add(new EventNode("DEPARTED", tCurrent, hub, "已离开 " + hub));

            // 到达该枢纽
            tCurrent = tCurrent.plusHours(6);
            nodes.add(new EventNode("ARRIVED", tCurrent, hub, "已到达 " + hub));
        }

        // 到达目的地城市网点
        tCurrent = tCurrent.plusHours(12);
        String destHub = destCity != null ? destCity + " 派送站" : "目的地派送站";
        nodes.add(new EventNode("ARRIVED", tCurrent, destHub, "已到达 " + destHub));

        // 派送中
        tCurrent = tCurrent.plusHours(6);
        nodes.add(new EventNode("OUT_FOR_DELIVERY", tCurrent, destHub, String.format("%s 快递员正在为您派送。", companyName != null ? companyName : "快递公司")));

        // 可选暂存
        tCurrent = tCurrent.plusHours(2);
        nodes.add(new EventNode("IN_STORAGE", tCurrent, destHub, "已暂存至代收点，请尽快领取"));

        // 签收（最新）
        tCurrent = tCurrent.plusHours(4);
        nodes.add(new EventNode("SIGNED", tCurrent, destAddrText != null ? destAddrText : destHub, "您的快件已签收，签收人代收"));

        // 防止未来时间（如果最后签收时间在未来，按 now 回退并均匀移动）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = nodes.get(nodes.size() - 1).time;
        if (last.isAfter(now)) {
            Duration totalDuration = Duration.between(nodes.get(0).time, last);
            long totalH = Math.max(1, totalDuration.toHours());
            // 计算缩放因子，将最后时间映射为 now，其他节点按比例回退
            long shiftSeconds = Duration.between(now, last).getSeconds();
            // 简单地把每个节点的时间统一往前移动 shiftSeconds
            for (EventNode n : nodes) {
                n.time = n.time.minusSeconds(shiftSeconds);
            }
        }

        // 将 EventNode 转为 TrackEvent，且按“最新第一”排序（很多前端期望最新在前）
        List<TrackEvent> history = new ArrayList<>();
        for (int i = nodes.size() - 1; i >= 0; i--) {
            EventNode n = nodes.get(i);
            history.add(new TrackEvent(format(n.time), n.status, n.location, n.desc));
        }
        return history;
    }

    /**
     * 根据目的地省/市生成“枢纽链路”（从发货仓到目的地派送站）—— 返回一个 hub 列表（第一个通常为发货仓）
     */
    private List<String> buildHubsChain(String destProvince, String destCity) {
        // 统一为小写便于匹配
        String prov = destProvince == null ? "" : destProvince.toLowerCase();

        // 定义各大区域的代表枢纽链（可以按需扩展或替换为配置）
        if (prov.contains("北京") || prov.contains("天津") || prov.contains("河北") || prov.contains("内蒙")) {
            return Arrays.asList("广东发货仓", "华南枢纽", "华东枢纽", "华北枢纽", (destCity != null ? destCity + " 派送站" : "北京 派送站"));
        }

        if (prov.contains("上海") || prov.contains("江苏") || prov.contains("浙江") || prov.contains("安徽") || prov.contains("江西")) {
            return Arrays.asList("广东发货仓", "华南枢纽", "华东枢纽", (destCity != null ? destCity + " 派送站" : "目的地 派送站"));
        }

        if (prov.contains("四川") || prov.contains("重庆") || prov.contains("贵州") || prov.contains("云南")) {
            return Arrays.asList("广东发货仓", "华南枢纽", "西南枢纽", (destCity != null ? destCity + " 派送站" : "目的地 派送站"));
        }

        if (prov.contains("陕西") || prov.contains("甘肃") || prov.contains("宁夏") || prov.contains("青海") || prov.contains("新疆")) {
            return Arrays.asList("广东发货仓", "华南枢纽", "西北枢纽", (destCity != null ? destCity + " 派送站" : "目的地 派送站"));
        }

        if (prov.contains("广东") || prov.contains("广西") || prov.contains("福建") || prov.contains("海南")) {
            return Arrays.asList("广东发货仓", "华南枢纽", (destCity != null ? destCity + " 派送站" : "目的地 派送站"));
        }

        // default fallback chain
        return Arrays.asList("广东发货仓", "华南枢纽", "华东枢纽", "华北枢纽", (destCity != null ? destCity + " 派送站" : "目的地 派送站"));
    }

    private String generateTrackingNoByCompany(String code) {
        Map<String, String> prefixMap = Map.of(
                "shunfeng", "SF",
                "yunda", "YD",
                "yuantong", "YT",
                "shentong", "ST",
                "zhongtong", "ZT",
                "huitongkuaidi", "HTKD",
                "tiantian", "TT",
                "zhaijisong", "ZJS",
                "yzguonei", "YZ"
        );
        String low = code == null ? "" : code.toLowerCase();
        String prefix = prefixMap.getOrDefault(low, !low.isEmpty() ? low.substring(0, Math.min(3, low.length())).toUpperCase() : "EXP");
        String ts = String.valueOf(System.currentTimeMillis());
        String tail = ts.length() > 8 ? ts.substring(ts.length() - 8) : ts;
        int rnd = (int) (Math.random() * 9000) + 1000;
        return prefix + tail + rnd;
    }

    private String format(LocalDateTime dt) {
        return dt.format(TIME_FMT);
    }

    private static class EventNode {
        String status;
        LocalDateTime time;
        String location;
        String desc;

        EventNode(String status, LocalDateTime time, String location, String desc) {
            this.status = status;
            this.time = time;
            this.location = location;
            this.desc = desc;
        }
    }
}

package com.bik.flower_shop.pojo.vo;

import com.bik.flower_shop.pojo.dto.OrderAddressDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author bik
 */
@Data
public class OrderDetailVO {

    private Integer orderId;
    private String orderNo;
    private BigDecimal subtotal;
    private BigDecimal shipping;
    private BigDecimal discount;
    private BigDecimal totalPrice;

    private String shipStatus;
    private Integer createTime;
    private String payStatus;

    private OrderAddressDTO address;
    private List<OrderItemVO> items;
    // 订单过期时间（秒）
    private Integer expireTime;
}

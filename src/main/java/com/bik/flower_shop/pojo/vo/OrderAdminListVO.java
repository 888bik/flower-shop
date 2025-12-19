package com.bik.flower_shop.pojo.vo;

import com.bik.flower_shop.pojo.dto.OrderAddressDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@Data
public class OrderAdminListVO {
    private Integer orderId;
    private String orderNo;

    private UserSimpleVO user;

    private OrderAddressDTO address;

    private List<OrderAdminItemVO> items;

    // 价格/金额相关
    private BigDecimal totalPrice;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discount;

    // 支付 / 物流 / 退款
    private String payStatus;
    private Integer paidTime;
    private String paymentMethod;
    private String paymentNo;
    private String refundStatus;
    private String refundNo;

    private String shipStatus;
    private Map<String, Object> shipData;

    // 其它后台字段
    private String remark;
    private Boolean closed;
    private Map<String, Object> extra;
    private Boolean reviewed;

    private Integer createTime;
}

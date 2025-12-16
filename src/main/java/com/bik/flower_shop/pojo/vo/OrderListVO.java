package com.bik.flower_shop.pojo.vo;

import com.bik.flower_shop.pojo.dto.OrderAddressDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author bik
 */
@Data
public class OrderListVO {

    private Integer orderId;
    private String orderNo;
    private BigDecimal totalPrice;
    private String shipStatus;
    private Integer createTime;

    private String payStatus;

    /**
     * 反序列化后的地址
     */
    private OrderAddressDTO address;

    private List<OrderItemVO> items;

    private BigDecimal subtotal;

    private BigDecimal shippingFee;

    private BigDecimal discount;

    private Integer expireTime;
}

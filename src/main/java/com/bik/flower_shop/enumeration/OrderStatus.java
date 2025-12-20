package com.bik.flower_shop.enumeration;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderStatus {

    UNPAID("unpaid", "待支付"),
    PAID("paid", "已支付"),
    SHIPPED("shipped", "已发货"),
    RECEIVED("received", "已收货"),
    FINISHED("finished", "已完成"),
    CLOSED("closed", "已关闭"),
    REFUNDING("refunding", "退款中");

    private final String code;
    private final String desc;
}

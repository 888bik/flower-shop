package com.bik.flower_shop.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author bik
 */
@Data
public class UserListDTO {

    private Integer id;

    private String username;

    private String nickname;

    private String avatar;

    private String phone;

    private String email;

    /** 用户状态 0/1 */
    private Byte status;

    /** 用户等级 */
    private Integer userLevelId;
    private String userLevelName;

    /** 推广数据 */
    private Integer shareNum;
    private Integer shareOrderNum;

    /** 金额数据 */
    private BigDecimal orderPrice;
    private BigDecimal commission;

    private Integer createTime;
}

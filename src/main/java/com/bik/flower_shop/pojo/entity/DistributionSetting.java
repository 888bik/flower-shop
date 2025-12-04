package com.bik.flower_shop.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 分销设置
 * </p>
 *
 * @author bik
 * @since 2025-12-04
 */
@Getter
@Setter
@TableName("distribution_setting")
public class DistributionSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 分销启用:0禁用1启用
     */
    @TableField("distribution_open")
    private Boolean distributionOpen;

    /**
     * 一级返佣比例
     */
    @TableField("store_first_rebate")
    private Integer storeFirstRebate;

    /**
     * 二级返佣比例
     */
    @TableField("store_second_rebate")
    private Integer storeSecondRebate;

    /**
     * 分销海报图
     */
    @TableField("spread_banners")
    private String spreadBanners;

    /**
     * 自购返佣:0否1是
     */
    @TableField("is_self_brokerage")
    private Boolean isSelfBrokerage;

    /**
     * 结算时间（单位：天）
     */
    @TableField("settlement_days")
    private Integer settlementDays;

    /**
     * 佣金到账方式:hand手动,wx微信
     */
    @TableField("brokerage_method")
    private String brokerageMethod;
}

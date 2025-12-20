package com.bik.flower_shop.pojo.vo;

import lombok.Data;

/**
 * @author bik
 */
@Data
public class DashboardPanelVO {

    private String title;
    private Object value;
    private String unit;
    private String unitColor;

    private String subTitle;
    private Object subValue;
    private String subUnit;
}

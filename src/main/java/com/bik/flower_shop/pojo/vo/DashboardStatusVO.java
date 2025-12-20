package com.bik.flower_shop.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author bik
 */
@Data
public class DashboardStatusVO {

    private List<StatusCountVO> goods;
    private List<StatusCountVO> order;
}

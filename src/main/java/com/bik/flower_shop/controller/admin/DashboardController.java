package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.vo.DashboardPanelResponseVO;
import com.bik.flower_shop.pojo.vo.DashboardPanelVO;
import com.bik.flower_shop.pojo.vo.DashboardStatusVO;
import com.bik.flower_shop.pojo.vo.DashboardTrendVO;
import com.bik.flower_shop.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@AuthRequired(role = "admin")
@RequestMapping("/admin/dashboard")
public class DashboardController {


    private final DashboardService dashboardService;

    /**
     * 获取面板数据1
     */
    @GetMapping("/panels")
    public ApiResult<DashboardPanelResponseVO> panels() {
        return ApiResult.ok(dashboardService.getPanels());
    }

    /**
     * 获取面板数据2
     */
    @GetMapping("/status")
    public ApiResult<DashboardStatusVO> status() {
        return ApiResult.ok(dashboardService.getStatus());
    }

    /**
     * 获取面板数据3
     */
    @GetMapping("/trend")
    public ApiResult<DashboardTrendVO> orderTrend(@RequestParam(defaultValue = "week") String type) {
        return ApiResult.ok(dashboardService.getRecentOrderTrend(type));
    }
}

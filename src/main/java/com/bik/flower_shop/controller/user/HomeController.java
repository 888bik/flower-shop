package com.bik.flower_shop.controller.user;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.entity.HomeFloor;
import com.bik.flower_shop.pojo.vo.HomeFloorVO;
import com.bik.flower_shop.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author bik
 */
@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/floors")
    public ApiResult<List<HomeFloorVO>> homeFloors() {
        return ApiResult.ok(homeService.getHomeFloors());
    }
}

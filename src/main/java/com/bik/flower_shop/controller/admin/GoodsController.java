package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.GoodsQueryDTO;
import com.bik.flower_shop.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequestMapping("/admin/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    /**
     * 获取商品列表
     */
    @GetMapping("/{page}")
    public ApiResult<Map<String, Object>> getGoodsList(@RequestBody GoodsQueryDTO dto,
                                               @RequestHeader(value = "token", required = false) String token) {
        Map<String, Object> result = goodsService.listGoods(dto);
        return ApiResult.ok(result);
    }

}

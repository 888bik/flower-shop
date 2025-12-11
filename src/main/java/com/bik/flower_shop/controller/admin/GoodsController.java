package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.*;
import com.bik.flower_shop.pojo.entity.Goods;
import com.bik.flower_shop.pojo.entity.GoodsBanner;
import com.bik.flower_shop.pojo.vo.GoodsVO;
import com.bik.flower_shop.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ApiResult<Map<String, Object>> getGoodsList(
            @PathVariable Integer page,
            @RequestParam(value = "tab") String tab,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "categoryIds", required = false) String categoryIds
    ) {
        GoodsQueryDTO dto = new GoodsQueryDTO();
        dto.setPage(page);
        dto.setTab(tab);
        dto.setTitle(title);
        dto.setLimit(limit);

        if (categoryIds != null && !categoryIds.isBlank()) {
            // categoryIds 可能格式 "1,2,3" 或 "1"，做简单解析
            List<Integer> catIds = Arrays.stream(categoryIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
            dto.setCategoryIds(catIds);
        }

        Map<String, Object> result = goodsService.listGoods(dto);
        return ApiResult.ok(result);
    }

    /**
     * 批量删除商品(软删除)
     *
     * @param request 请求体包含 ids 数组
     */
    @PostMapping("/delete_all")
    public ApiResult<String> deleteGoods(@RequestBody DeleteGoodsRequest request) {
        goodsService.softDeleteGoods(request.getIds());
        return ApiResult.ok("ok");
    }

    /**
     * 批量恢复商品
     *
     * @param request 请求体包含 ids 数组
     */
    @PostMapping("/restore")
    public ApiResult<String> restore(@RequestBody DeleteGoodsRequest request) {
        goodsService.restoreGoods(request.getIds());
        return ApiResult.ok("ok");
    }

    /**
     * 批量删除商品
     *
     * @param request 请求体包含 ids 数组
     */
    @PostMapping("/delete_force")
    public ApiResult<String> deleteForce(@RequestBody DeleteGoodsRequest request) {
        goodsService.deleteForceGoods(request.getIds());
        return ApiResult.ok("ok");
    }


    /**
     * 添加商品
     */
    @PostMapping
    public ApiResult<Goods> createGoods(@RequestBody Goods goods){
        Goods saved = goodsService.saveGoods(goods);
        return ApiResult.ok(saved);
    }


    /**
     * 更新商品信息
     */
    @PostMapping("/{id}")
    public ApiResult<String> updateGoods(@PathVariable Integer id, @RequestBody UpdateGoodsDTO dto,
                                         @RequestHeader(value = "token", required = false) String token) {
        boolean success = goodsService.updateGoods(id, dto);
        if (success) {
            return ApiResult.ok("ok");
        } else {
            return ApiResult.fail("修改失败，商品不存在或参数错误");
        }
    }

    /**
     * 批量上架/下架商品
     */
    @PostMapping("/changestatus")
    public ApiResult<Integer> changeGoodsStatus(@RequestBody ChangeGoodsStatusDTO dto,
                                                @RequestHeader(value = "token", required = false) String token) {
        int updatedCount = goodsService.changeGoodsStatus(dto);
        return ApiResult.ok(updatedCount);
    }

    /**
     * 查看单个商品
     */
    @GetMapping("/read/{id}")
    public ApiResult<GoodsVO> readGoods(@PathVariable("id") Integer id,
                                        @RequestHeader(value = "token", required = false) String token) {
        GoodsVO vo = goodsService.getGoodsById(id);
        return ApiResult.ok(vo);
    }

    /**
     * 设置商品轮播图
     */
    @PostMapping("/banners/{id}")
    public ApiResult<List<GoodsBanner>> setGoodsBanners(@PathVariable("id") Integer goodsId,
                                                        @RequestBody BannerDTO dto,
                                                        @RequestHeader(value = "token", required = false) String token) {
        List<GoodsBanner> result = goodsService.setGoodsBanners(goodsId, dto.getBanners());
        return ApiResult.ok(result);
    }

    /**
     * 更新商品详情
     */
    @PostMapping("/updateContent/{id}")
    public ApiResult<Void> updateGoodsContent(
            @PathVariable Integer id,
            @RequestBody GoodsContentDTO dto,
            @RequestHeader(value = "token", required = false) String token) {

        goodsService.updateContent(id, dto.getContent());

        return ApiResult.ok(null);
    }

    /**
     * 审核商品（通过自动上架，拒绝自动下架）
     */
    @PostMapping("/{id}/check")
    public ApiResult<Boolean> checkGoods(
            @PathVariable Integer id,
            @RequestBody CheckGoodsDTO dto) {

        goodsService.checkGoods(id, dto.getIscheck());
        return ApiResult.ok(true);
    }
}

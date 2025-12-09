package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.*;
import com.bik.flower_shop.pojo.entity.GoodsSkusCard;
import com.bik.flower_shop.pojo.entity.GoodsSkusCardValue;
import com.bik.flower_shop.pojo.vo.GoodsSkusCardVO;
import com.bik.flower_shop.service.GoodsSkusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author: bik
 * Date: 2025-12-07
 * Time: 21:13
 */
@RestController
@RequestMapping("/admin/goods_skus_card")
@RequiredArgsConstructor
public class GoodsSkusController {


    private final GoodsSkusService goodsSkusService;


    /**
     * 创建商品规格项
     */
    @PostMapping
    public ApiResult<Map<String, Object>> createGoodsSkusCard(@RequestBody AddGoodsSkusCardDTO dto) {
        try {
            GoodsSkusCard card = goodsSkusService.createGoodsSkusCard(dto);

            Map<String, Object> data = new HashMap<>();
            data.put("goodsId", card.getGoodsId());
            data.put("name", card.getName());
            data.put("order", card.getOrder());
            data.put("type", (card.getType()));
            data.put("id", card.getId());

            return ApiResult.ok(data);
        } catch (RuntimeException ex) {
            return ApiResult.fail(ex.getMessage());
        } catch (Exception ex) {
            return ApiResult.fail("服务器错误");
        }
    }


    /**
     * 更新商品规格项的值
     */
    @PostMapping("/updateskus/{id}")
    public ApiResult<Boolean> updateGoodsSkusCard(@PathVariable Long id, @RequestBody UpdateSkusDTO dto) {
        goodsSkusService.updateGoodsSkus(id, dto);
        return ApiResult.ok(true);
    }


    /**
     * 删除商品规格项
     */
    @PostMapping("/{id}/delete")
    public ApiResult<Boolean> deleteGoodsSkusCard(@PathVariable Integer id) {
        boolean ok = goodsSkusService.deleteGoodsSkusCard(id);
        if (ok) {
            return ApiResult.ok(true);
        }
        return ApiResult.fail("删除失败，规格不存在");
    }


    /**
     * 更新商品规格项
     */
    @PostMapping("/{id}")
    public ApiResult<GoodsSkusCardVO> updateGoodsSkusCard(
            @PathVariable("id") Integer id,
            @RequestBody UpdateGoodsSkusCardDTO dto) {

        GoodsSkusCardVO vo = goodsSkusService.updateGoodsSkusCard(id, dto);
        return ApiResult.ok(vo);
    }

    /**
     * 排序商品规格项
     */
    @PostMapping("/sort")
    public ApiResult<Boolean> sortGoodsSkusCard(@RequestBody UpdateGoodsSkusOrderDTO dto) {
        try {
            boolean ok = goodsSkusService.sortGoodsSkus(dto);
            if (ok) {
                return ApiResult.ok(true);
            } else {
                return ApiResult.fail("排序失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResult.fail("服务器错误");
        }
    }

    /**
     * 添加商品规格值
     */
    @PostMapping("/value")
    public ApiResult<GoodsSkusCardValue> createSkusCardValue(@RequestBody CreateGoodsSkusCardValueDTO dto) {
        GoodsSkusCardValue created = goodsSkusService.createGoodsSkusCardValue(dto);
        return ApiResult.ok(created);
    }

    /**
     * 删除商品规格值
     */
    @PostMapping("/{id}/deleteValue")
    public ApiResult<Boolean> deleteSkusCardValue(@PathVariable Integer id) {
        boolean ok = goodsSkusService.deleteGoodsSkusCardValue(id);
        return ApiResult.ok(ok);
    }

    /**
     * 更新商品规格值
     */
    @PostMapping("/{id}/updateValue")
    public ApiResult<Boolean> updateGoodsSkusCardValue(
            @PathVariable Integer id,
            @RequestBody UpdateGoodsSkusCardValueDTO dto) {

        boolean ok = goodsSkusService.updateGoodsSkusCardValue(id, dto);
        return ApiResult.ok(ok);
    }


    /**
     * 设置商品规格值
     */
    @PostMapping("/{id}/set")
    public ApiResult<Map<String, Object>> setGoodsSkusCardAndValue(
            @PathVariable Integer id,
            @RequestBody SetGoodsSkusCardDTO dto) {

        Map<String, Object> result = goodsSkusService.setGoodsSkusCardValues(id, dto);
        return ApiResult.ok(result);
    }
}
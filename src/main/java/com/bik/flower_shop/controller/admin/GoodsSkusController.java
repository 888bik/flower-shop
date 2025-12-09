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


    @PostMapping("/updateskus/{id}")
    public ApiResult<Boolean> updateGoodsSkus(@PathVariable Long id, @RequestBody UpdateSkusDTO dto) {
        goodsSkusService.updateGoodsSkus(id, dto);
        return ApiResult.ok(true);
    }


    @PostMapping("/{id}/delete")
    public ApiResult<Boolean> deleteGoodsSkus(@PathVariable Integer id) {
        boolean ok = goodsSkusService.deleteGoodsSkusCard(id);
        if (ok) {
            return ApiResult.ok(true);
        }
        return ApiResult.fail("删除失败，规格不存在");
    }


    @PostMapping("/{id}")
    public ApiResult<GoodsSkusCardVO> updateGoodsSkusCard(
            @PathVariable("id") Integer id,
            @RequestBody UpdateGoodsSkusCardDTO dto) {

        GoodsSkusCardVO vo = goodsSkusService.updateGoodsSkusCard(id, dto);
        return ApiResult.ok(vo);
    }

    @PostMapping("/sort")
    public ApiResult<Boolean> sortGoodsSkus(@RequestBody UpdateGoodsSkusOrderDTO dto) {
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

    @PostMapping("/value")
    public ApiResult<GoodsSkusCardValue> createSkusCardValue(@RequestBody CreateGoodsSkusCardValueDTO dto) {
        GoodsSkusCardValue created = goodsSkusService.createGoodsSkusCardValue(dto);
        return ApiResult.ok(created);
    }

    @PostMapping("/{id}/deleteValue")
    public ApiResult<Boolean> delete(@PathVariable Integer id) {
        boolean ok = goodsSkusService.deleteGoodsSkusCardValue(id);
        return ApiResult.ok(ok);
    }

    @PostMapping("/{id}/updateValue")
    public ApiResult<Boolean> updateGoodsSkusCardValue(
            @PathVariable Integer id,
            @RequestBody UpdateGoodsSkusCardValueDTO dto) {

        boolean ok = goodsSkusService.updateGoodsSkusCardValue(id, dto);
        return ApiResult.ok(ok);
    }
}
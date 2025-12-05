package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.SkusDTO;
import com.bik.flower_shop.pojo.entity.Skus;
import com.bik.flower_shop.service.SkusService;
import com.bik.flower_shop.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/skus")
public class SkusController {

    private final SkusService skusService;

    /**
     * 增加商品规格
     */
    @PostMapping
    public ApiResult<Map<String, Object>> createSkus(
            @RequestHeader("token") String token,
            @ModelAttribute SkusDTO dto,
            @RequestParam("defaults") String defaultParam
    ) {

        dto.setDefaults(defaultParam);
        Skus created = skusService.createSkus(dto.getStatus(), dto.getName(), dto.getOrder(), dto.getDefaults());
        Map<String, Object> data = new HashMap<>();
        data.put("status", String.valueOf(created.getStatus() ? 1 : 0));
        data.put("name", created.getName());
        data.put("order", String.valueOf(created.getOrder()));
        data.put("defaults", created.getDefaults());
        data.put("create_time", TimeUtils.format(created.getCreateTime()));
        data.put("update_time", TimeUtils.format(created.getUpdateTime()));
        data.put("id", String.valueOf(created.getId()));

        return ApiResult.ok(data);
    }

    @PostMapping("/{id}")
    public ApiResult<Boolean> updateSkus(
            @RequestHeader("token") String token,
            @PathVariable("id") Integer id,
            @ModelAttribute SkusDTO dto
    ) {
        System.out.println("updateSkus: " + dto);
        boolean success = skusService.updateSkus(id, dto.getStatus(), dto.getName(), dto.getOrder(), dto.getDefaults());
        return ApiResult.ok(success);
    }

    /**
     * 修改商品规格状态
     */
    @PostMapping("/{id}/update_status")
    public ApiResult<Boolean> updateSkusStatus(
            @RequestHeader("token") String token,
            @PathVariable("id") Integer id,
            @RequestParam("status") Integer status
    ) {
        boolean success = skusService.updateSkusStatus(id, status);
        return ApiResult.ok(success);
    }


    /**
     * 商品规格列表（分页）
     */
    @GetMapping("/{page}")
    public ApiResult<Map<String, Object>> listSkus(
            @RequestHeader("token") String token,
            @PathVariable("page") Integer page
    ) {
        int pageSize = 10;
        var result = skusService.getListSkus(page, pageSize);

        return ApiResult.ok(result);
    }

    /**
     * 批量删除商品规格
     */
    @PostMapping("/delete_all")
    public ApiResult<Integer> deleteAllSkus(
            @RequestHeader("token") String token,
            @RequestBody Map<String, Object> body
    ) {
        // 获取 JSON 数组 ids
        Object obj = body.get("ids");
        if (!(obj instanceof java.util.List<?> ids)) {
            return ApiResult.<Integer>fail("参数错误：必须包含 ids 数组", 20000);
        }

        // 转成整数数组
        var idList = ids.stream()
                .map(item -> Integer.parseInt(item.toString()))
                .toList();

        int deletedCount = skusService.deleteAllSkus(idList);
        return ApiResult.<Integer>ok(deletedCount);
    }


}

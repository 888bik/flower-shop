package com.bik.flower_shop.controller.user;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.MallQueryDTO;
import com.bik.flower_shop.pojo.entity.Category;
import com.bik.flower_shop.service.MallService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequestMapping("/mall")
@RequiredArgsConstructor
public class MallController {

    private final MallService mallService;

    @GetMapping("/goods")
    public ApiResult<Map<String, Object>> listMallGoods(
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "limit", required = false, defaultValue = "12") Integer limit,
            @RequestParam(value = "categoryIds", required = false) Integer categoryId,
            @RequestParam(value = "title", required = false) String title
    ) {
        MallQueryDTO dto = new MallQueryDTO();
        dto.setPage(page);
        dto.setLimit(limit);
        dto.setCategoryId(categoryId);
        dto.setTitle(title);
        Map<String, Object> data = mallService.listMallGoods(dto);
        return ApiResult.ok(data);
    }

    @GetMapping("/categories")
    public ApiResult<List<Category>> listCategories(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "parentId", required = false) Integer parentId,
            @RequestParam(value = "status", required = false) Byte status
    ) {
        List<Category> categories = mallService.listCategories(type, parentId, status);
        return ApiResult.ok(categories);
    }
}

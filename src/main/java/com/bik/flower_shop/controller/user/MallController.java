package com.bik.flower_shop.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.context.BaseController;
import com.bik.flower_shop.pojo.dto.GoodsDetailDTO;
import com.bik.flower_shop.pojo.dto.GoodsSearchDTO;
import com.bik.flower_shop.pojo.dto.MallQueryDTO;
import com.bik.flower_shop.pojo.entity.Category;
import com.bik.flower_shop.pojo.entity.Goods;
import com.bik.flower_shop.pojo.vo.GoodsVO;
import com.bik.flower_shop.service.GoodsService;
import com.bik.flower_shop.service.MallService;
import com.bik.flower_shop.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequestMapping("/mall")
@RequiredArgsConstructor
public class MallController extends BaseController {

    private final TokenService tokenService;

    @Override
    protected TokenService getTokenService() {
        return tokenService;
    }

    private final MallService mallService;
    private final GoodsService goodsService;

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

    @GetMapping("/goods/{id}")
    public ApiResult<GoodsDetailDTO> frontendGoods(@PathVariable Integer id) {
        // 获取当前用户id
        Integer userId = getCurrentUserId();
        GoodsVO vo = goodsService.getGoodsById(id);
        GoodsDetailDTO dto = mallService.toFrontendDto(vo, userId);
        return ApiResult.ok(dto);
    }

    /**
     * 搜索商品
     * GET /goods/search?keyword=xxx&page=1&limit=12
     */
    @GetMapping("/search")
    public ApiResult<Page<GoodsSearchDTO>> search(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "12") Integer limit) {

        Page<GoodsSearchDTO> result = mallService.searchGoods(keyword, page, limit);
        return ApiResult.ok(result);
    }
}

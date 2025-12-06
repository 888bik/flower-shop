package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.ImageClassUpdateDTO;
import com.bik.flower_shop.pojo.entity.ImageClass;
import com.bik.flower_shop.service.ImageClassService;
import com.bik.flower_shop.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequestMapping("/admin/image_class")
@RequiredArgsConstructor
public class ImageClassController {

    private final ImageClassService imageClassService;

    private final ImageService imageService;

    /**
     * 获取图库分类列表
     */
    @GetMapping("/{page}")
    public ApiResult<Map<String, Object>> getImageClassList(@PathVariable int page,
                                                            @RequestParam(required = false, defaultValue = "10") int limit) {
        Map<String, Object> result = imageClassService.listAllWithImageCount(page, limit);
        return ApiResult.ok(result);
    }

    /**
     * 获取指定分类下的图片列表
     */
    @GetMapping("/{id}/image/{page}")
    public ApiResult<Map<String, Object>> getImageListByClass(@PathVariable("id") Integer classId,
                                                              @PathVariable("page") int page,
                                                              @RequestParam(required = false, defaultValue = "10") int limit) {
        Map<String, Object> result = imageService.listByClassId(classId, page, limit);
        return ApiResult.ok(result);
    }

    /**
     * 创建图库分类
     */
    @PostMapping
    public ApiResult<Map<String, Object>> createImageClass(@RequestBody ImageClass imageClass) {
        ImageClass created = imageClassService.createImageClass(imageClass);

        Map<String, Object> result = new HashMap<>();
        result.put("id", created.getId());
        result.put("name", created.getName());
        result.put("order", created.getOrder());

        return ApiResult.ok(result);
    }

    /**
     * 修改图库分类
     */
    @PostMapping("/{id}")
    public ApiResult<Boolean> updateImageClass(@PathVariable Integer id,
                                               @RequestBody ImageClassUpdateDTO dto) {
        boolean success = imageClassService.updateImageClassById(id, dto.getName(), dto.getOrder());
        return ApiResult.ok(success);
    }

    /**
     * 删除图库分类
     */
    @PostMapping("/{id}/delete")
    public ApiResult<Boolean> deleteImageClass(@PathVariable Integer id,
                                               @RequestHeader("token") String token) {
        // 暂时不校验 token，直接调用 Service
        boolean result = imageClassService.deleteImageClassById(id);
        return ApiResult.ok(result);
    }
}

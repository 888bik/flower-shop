package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
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

    @GetMapping("/{page}")
    public ApiResult<Map<String, Object>> list(@PathVariable int page,
                                               @RequestParam(required = false, defaultValue = "10") int limit) {
        List<ImageClass> list = imageClassService.listAllWithImageCount();
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalCount", list.size());
        return ApiResult.ok(result);
    }

    @GetMapping("/{id}/image/{page}")
    public ApiResult<Map<String, Object>> listImagesByClass(@PathVariable("id") Integer classId,
                                                            @PathVariable("page") int page,
                                                            @RequestParam(required = false, defaultValue = "10") int limit) {
        Map<String, Object> result = imageService.listByClassId(classId, page, limit);
        return ApiResult.ok(result);
    }

    @PostMapping
    public ApiResult<Map<String, Object>> addImageClass(@RequestBody ImageClass imageClass) {
        ImageClass created = imageClassService.createImageClass(imageClass);

        Map<String, Object> result = new HashMap<>();
        result.put("id", created.getId());
        result.put("name", created.getName());
        result.put("order", created.getOrder());

        return ApiResult.ok(result);
    }

    @PostMapping("/{id}")
    public ApiResult<Boolean> updateImageClass(@PathVariable Integer id,
                                               @RequestParam String name,
                                               @RequestParam Integer order) {
        boolean success = imageClassService.updateImageClassById(id, name, order);
        return ApiResult.ok(success);
    }


    @PostMapping("/{id}/delete")
    public ApiResult<Boolean> delete(@PathVariable Integer id,
                                     @RequestHeader("token") String token) {
        // 暂时不校验 token，直接调用 Service
        boolean result = imageClassService.deleteById(id);
        return ApiResult.ok(result);
    }
}

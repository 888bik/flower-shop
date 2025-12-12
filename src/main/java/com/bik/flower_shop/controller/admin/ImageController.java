package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.entity.Image;
import com.bik.flower_shop.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@RequiredArgsConstructor
@RestController
@AuthRequired(role = "admin")
@RequestMapping("/admin/image")
public class ImageController {

    private final ImageService imageService;

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public ApiResult<List<Image>> uploadImages(@RequestHeader("token") String token,
                                               @RequestParam("image_class_id") Integer imageClassId,
                                               @RequestParam("img") MultipartFile[] files) throws Exception {
        List<Image> images = imageService.uploadImages(imageClassId, files);
        return ApiResult.ok(images);
    }

    /**
     * 删除图片
     */
    @PostMapping("/delete_all")
    public ApiResult<Boolean> deleteImages(@RequestHeader("token") String token,
                                           @RequestBody Map<String, List<Integer>> body) {
        List<Integer> ids = body.get("ids");
        boolean result = imageService.deleteImagesByIds(ids);
        return ApiResult.ok(result);
    }

    @PostMapping("/{id}")
    public ApiResult<Boolean> renameImage(
            @RequestHeader("token") String token,
            @PathVariable("id") Integer id,
            @RequestBody Map<String, String> body
    ) {
        String name = body.get("name");
        boolean result = imageService.updateImageName(id, name);
        return ApiResult.ok(result);
    }

}
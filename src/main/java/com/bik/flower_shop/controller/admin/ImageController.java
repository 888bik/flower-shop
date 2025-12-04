package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.entity.Image;
import com.bik.flower_shop.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/image")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ApiResult<List<Image>> uploadImages(@RequestHeader("token") String token,
                                               @RequestParam("image_class_id") Integer imageClassId,
                                               @RequestParam("img[]") MultipartFile[] files) throws Exception {
        List<Image> images = imageService.uploadImages(imageClassId, files);
        return ApiResult.ok(images);
    }


    @PostMapping("/delete_all")
    public ApiResult<Boolean> deleteImages(@RequestHeader("token") String token,
                                           @RequestBody Map<String, List<Integer>> body) {
        List<Integer> ids = body.get("ids");
        boolean result = imageService.deleteImagesByIds(ids);
        return ApiResult.ok(result);
    }
}
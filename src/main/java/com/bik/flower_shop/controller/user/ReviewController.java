package com.bik.flower_shop.controller.user;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.ReviewPageResponse;
import com.bik.flower_shop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/review")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{goodsId}")
    public ApiResult<ReviewPageResponse> getReviews(
            @PathVariable Integer goodsId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String filter
    ) {
        ReviewPageResponse response =  reviewService.getReviews(goodsId, page, pageSize, filter);
        return ApiResult.ok(response);
    }
}

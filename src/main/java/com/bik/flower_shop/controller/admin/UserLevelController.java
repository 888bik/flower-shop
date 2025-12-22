package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.StatusDTO;
import com.bik.flower_shop.pojo.dto.UpdateUserLevelDTO;
import com.bik.flower_shop.pojo.dto.UpdateUserLevelStatusDTO;
import com.bik.flower_shop.pojo.dto.UserLevelAddDTO;
import com.bik.flower_shop.pojo.entity.UserLevel;
import com.bik.flower_shop.service.UserLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@AuthRequired(role = "admin")
@RequestMapping("/admin/user-level")
public class UserLevelController {

    private final UserLevelService userLevelService;

    /**
     * 获取会员等级列表
     */
    @GetMapping("/list")
    public ApiResult<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String keyword
    ) {
        Map<String, Object> data = userLevelService.getLevelList(page, limit,keyword);
        return ApiResult.ok(data);
    }

    /**
     * 添加会员等级
     */
    @PostMapping
    public ApiResult<Void> addLevel(@RequestBody UserLevelAddDTO dto) {
        userLevelService.addLevel(dto);
        return ApiResult.ok();
    }

    /**
     * 修改会员等级
     */
    @PostMapping("/update/{id}")
    public ApiResult<Void> updateLevel(@PathVariable Integer id, @RequestBody UpdateUserLevelDTO dto) {
        userLevelService.updateLevel(id, dto);
        return ApiResult.ok();
    }

    /**
     * 删除会员等级
     */
    @DeleteMapping("/delete/{id}")
    public ApiResult<Void> deleteLevel(@PathVariable Integer id) {
        userLevelService.deleteLevel(id);
        return ApiResult.ok();
    }


    /**
     * 修改会员等级状态
     */
    @PostMapping("/status")
    public ApiResult<Void> status(@RequestBody UpdateUserLevelStatusDTO dto) {
        userLevelService.updateUserLevelStatus(dto.getId(), dto.getStatus());
        return ApiResult.ok();
    }

}

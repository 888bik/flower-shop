package com.bik.flower_shop.controller.user;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.common.ListResult;
import com.bik.flower_shop.context.BaseController;
import com.bik.flower_shop.pojo.dto.*;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.service.TokenService;
import com.bik.flower_shop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * @author bik
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;
    private final TokenService tokenService;

    @Override
    protected TokenService getTokenService() {
        return tokenService;
    }

    /**
     * 用户登录
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<Map<String, String>> login(@RequestBody LoginDTO dto) {
        return ApiResult.ok(userService.loginAndGetTokens(dto.getUsername(), dto.getPassword()));
    }

    /**
     * 用户注册
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<Map<String, String>> register(@RequestBody RegisterDTO dto) {
        return ApiResult.ok(userService.register(dto));
    }

    /**
     * 获取用户资料（基础信息 + 扩展信息 + 地址 + 等级 + 最近账单）
     * 如果不传 userId，则尝试获取当前登录用户 id
     */
    @GetMapping("/profile")
    public ApiResult<Map<String, Object>> getUserProfile() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        Map<String, Object> data = userService.getUserProfile(userId);
        return ApiResult.ok(data);
    }


    @PostMapping("/upload-avatar")
    public ApiResult<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) throws Exception {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        // 调用 Service 上传头像
        String avatarUrl = userService.uploadAvatar(user, file);

        Map<String, String> result = new HashMap<>();
        result.put("url", avatarUrl);
        return ApiResult.ok(result);
    }


    @PostMapping("/update")
    public ApiResult<String> updateUserInfo(@RequestBody UpdateUserDTO dto
    ) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        // 调用 Service 更新
        userService.updateUserInfo(user.getId(), dto);

        return ApiResult.ok("更新成功");
    }

    @PostMapping("/logout")
    public ApiResult<String> logout() {
        User user = getCurrentUser();
        String accessToken = getAccessToken();
        String refreshToken = getRefreshToken();

        if (user == null || StringUtils.isBlank(accessToken) || StringUtils.isBlank(refreshToken)) {
            return ApiResult.fail("未登录或 token 无效");
        }

        userService.logout(accessToken, refreshToken);
        return ApiResult.ok("退出成功");
    }

    /**
     * 收藏/取消收藏商品
     */
    @PostMapping("/favorite/{id}")
    public ApiResult<FavoriteResultDTO> toggleFavorite(@PathVariable Integer id,
                                                       @RequestParam Boolean favorite) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        FavoriteResultDTO res = userService.toggleFavorite(user.getId(), id, favorite);
        return ApiResult.ok(res);
    }

    /**
     * 查询某商品是否已收藏
     */
    @GetMapping("/favorite/{id}")
    public ApiResult<Boolean> isFavorite(@PathVariable Integer id) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        return ApiResult.ok(userService.isFavorite(user.getId(), id));
    }

    /**
     * 获取当前用户收藏的商品列表
     */
    @GetMapping("/favorites")
    public ApiResult<ListResult<FavoriteGoodsVO>> getFavorites() {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        return ApiResult.ok(userService.getFavoriteGoodsList(user.getId()));
    }
}

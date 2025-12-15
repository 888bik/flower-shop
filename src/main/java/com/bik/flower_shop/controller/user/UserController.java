package com.bik.flower_shop.controller.user;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.LoginDTO;
import com.bik.flower_shop.pojo.dto.RegisterDTO;
import com.bik.flower_shop.pojo.dto.UpdateUserDTO;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.service.TokenService;
import com.bik.flower_shop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final HttpServletRequest request;


    /**
     * 用户登录
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<String> login(@RequestBody LoginDTO dto) {
        String token = userService.login(dto.getUsername(), dto.getPassword());
        return ApiResult.ok(token);
    }

    /**
     * 用户注册
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<String> register(@RequestBody RegisterDTO dto) {
        String token = userService.register(dto);
        return ApiResult.ok(token);
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
        System.out.println(user);

        // 调用 Service 上传头像
        String avatarUrl = userService.uploadAvatar(user, file);

        Map<String, String> result = new HashMap<>();
        result.put("url", avatarUrl);
        return ApiResult.ok(result);
    }


    @PostMapping("/update")
    public ApiResult<String> updateUserInfo(
            @RequestHeader("token") String token,
            @RequestBody UpdateUserDTO dto
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
    public ApiResult<String> logout(@RequestHeader("token") String token) {
        if (StringUtils.isBlank(token)) {
            return ApiResult.fail("未登录或 token 无效");
        }

        userService.logout(token);
        return ApiResult.ok("退出成功");
    }


    private User getCurrentUser() {
        // 从请求头取自定义 token
        String token = request.getHeader("token");
        if (token == null || token.isBlank()) {
            token = request.getParameter("token");
        }
        if (token == null || token.isBlank()) {
            return null;
        }
        // 返回 User 对象
        return tokenService.getUserByToken(token);
    }

    private Integer getCurrentUserId() {
        // 从请求头取自定义 token
        String token = request.getHeader("token");

        if (token == null || token.isBlank()) {
            // 尝试从 cookie 或 query 参数取 token
            token = request.getParameter("token");
        }

        if (token == null || token.isBlank()) {
            return null;
        }

        // 不需要去掉 "Bearer " 前缀，直接用 token 获取
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return null;
        }

        return user.getId();
    }
}

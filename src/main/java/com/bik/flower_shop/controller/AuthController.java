package com.bik.flower_shop.controller;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;

    @PostMapping("/refresh")
    public ApiResult<Map<String, String>> refreshToken(
            @RequestParam String refreshToken,
            @RequestParam String role // "user" 或 "admin"
    ) {
        Map<String, String> newTokens = tokenService.refreshByRefreshToken(refreshToken, role);
        if (newTokens == null) {
            return ApiResult.fail("refreshToken 无效或已过期");
        }
        return ApiResult.ok(newTokens);
    }
}

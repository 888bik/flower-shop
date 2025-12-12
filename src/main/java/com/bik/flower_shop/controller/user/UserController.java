package com.bik.flower_shop.controller.user;


import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.LoginDTO;
import com.bik.flower_shop.pojo.dto.RegisterDTO;
import com.bik.flower_shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 用户登录 */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<Map<String, String>> login(@RequestBody LoginDTO dto) {
        String token = userService.login(dto.getUsername(), dto.getPassword());
        return ApiResult.ok(Map.of("token", token));
    }

    /** 用户注册 */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<String> register(@RequestBody RegisterDTO dto) {
        String token = userService.register(dto);
        return ApiResult.ok(token);
    }

}

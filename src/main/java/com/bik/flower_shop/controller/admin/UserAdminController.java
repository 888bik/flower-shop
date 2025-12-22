package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.mapper.UserMapper;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@AuthRequired(role = "admin")
@RequestMapping("/admin/users")
public class UserAdminController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/list")
    public ApiResult<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResult.ok(userService.getUserList(page, limit, keyword));
    }

    @PostMapping("/status")
    public ApiResult<String> updateUserStatus(@RequestBody Map<String, Integer> request) {
        Integer id = request.get("id");
        Integer status = request.get("status");

        User user = userMapper.selectById(id);
        if (user == null) {
            return ApiResult.fail("用户不存在");
        }

        user.setStatus(status.byteValue());
        userMapper.updateById(user);
        return ApiResult.ok("修改成功");
    }


    @DeleteMapping("/{id}")
    public ApiResult<String> deleteUser(@PathVariable Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return ApiResult.fail("用户不存在");
        }
        userMapper.deleteById(id);
        return ApiResult.ok("删除成功");
    }
}

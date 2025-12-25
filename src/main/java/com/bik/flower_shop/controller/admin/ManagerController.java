package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.context.BaseController;
import com.bik.flower_shop.pojo.dto.LoginDTO;
import com.bik.flower_shop.pojo.dto.UpdateManagerDTO;
import com.bik.flower_shop.pojo.dto.UpdatePasswordDTO;
import com.bik.flower_shop.pojo.entity.Manager;
import com.bik.flower_shop.service.ManagerService;
import com.bik.flower_shop.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.Map;

/**
 * @author bik
 */
@RestController
@RequiredArgsConstructor
@AuthRequired(role = "admin")
@RequestMapping("/admin")
public class ManagerController extends BaseController {


    private final ManagerService managerService;
    private final TokenService tokenService;

    @Override
    protected TokenService getTokenService() {
        return tokenService;
    }

    /**
     * 管理员登陆
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<Map<String, String>> login(@RequestBody LoginDTO dto) {

        Map<String, String> tokens = managerService.loginAndGetTokens(dto.getUsername(), dto.getPassword());
        return ApiResult.ok(tokens);
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    public ApiResult<String> logout(@RequestHeader("accessToken") String accessToken,
                                    @RequestHeader("refreshToken") String refreshToken) {
        managerService.logout(accessToken, refreshToken);
        return ApiResult.ok("退出登录成功");
    }

    /**
     * 获取管理员列表
     */
    @AuthRequired(role = "admin")
    @GetMapping("/manager/{page}")
    public ApiResult<Map<String, Object>> getManagerList(
            @PathVariable Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResult.ok(managerService.getManagerList(page, limit, keyword));
    }

    /**
     * 创建管理员
     */
    @AuthRequired(role = "admin")
    @PostMapping("/manager")
    public ApiResult<Manager> createManager(@RequestBody Manager manager) {
        Manager created = managerService.createManager(manager);
        return ApiResult.ok(created);
    }


    /**
     * 获取管理员信息和权限
     */
    @AuthRequired(role = "admin")
    @PostMapping("/getinfo")
    public ApiResult<Map<String, Object>> getManagerInfo() {
        Manager currentManager = getCurrentManager();
        Map<String, Object> info = managerService.getInfoByToken(currentManager);
        return ApiResult.ok(info);
    }

    /**
     * 修改密码
     */
    @AuthRequired(role = "admin")
    @PostMapping("/updatepassword")
    public ApiResult<String> updatePassword(
            @RequestBody UpdatePasswordDTO dto) {

        Manager currentManager = getCurrentManager();

        // 调用 Service 修改密码
        managerService.updatePassword(
                currentManager,
                dto.getOldPassword(),
                dto.getPassword(),
                dto.getRePassword()
        );

        // 注销该用户所有 token（包括 access + refresh）
        tokenService.invalidateAllByManager(String.valueOf(currentManager.getId()), "admin");

        return ApiResult.ok("修改成功，请重新登录");
    }


    /**
     * 修改管理员状态
     */
    @AuthRequired(role = "admin")
    @PostMapping("/manager/{id}/update_status")
    public ApiResult<Boolean> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body
    ) {
        Integer status = (Integer) body.get("status");
        boolean result = managerService.updateStatus(id, status);
        return ApiResult.ok(result);
    }

    /**
     * 删除管理员
     */
    @AuthRequired(role = "admin")
    @PostMapping("/manager/{id}/delete")
    public ApiResult<Boolean> deleteManager(
            @PathVariable Integer id
    ) {
        boolean result = managerService.deleteManager(id);
        return ApiResult.ok(result);
    }

    /**
     * 修改管理员信息
     */
    @AuthRequired(role = "admin")
    @PostMapping("/manager/{id}")
    public ApiResult<Boolean> updateManager(
            @PathVariable("id") Integer id,
            @RequestBody UpdateManagerDTO dto) {

        Manager manager = new Manager();
        manager.setUsername(dto.getUsername());
        manager.setPassword(dto.getPassword());
        manager.setRoleId(dto.getRoleId());
        manager.setStatus(dto.getStatus() != null ? dto.getStatus().byteValue() : null);
        manager.setAvatar(dto.getAvatar());

        boolean success = managerService.updateManagerById(id, manager);
        return ApiResult.ok(success);
    }

}

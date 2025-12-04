package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.LoginDTO;
import com.bik.flower_shop.pojo.dto.UpdateManagerDTO;
import com.bik.flower_shop.pojo.dto.UpdatePasswordDTO;
import com.bik.flower_shop.pojo.entity.Manager;
import com.bik.flower_shop.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ManagerController {


    private final ManagerService managerService;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<Map<String, String>> login(@RequestBody LoginDTO dto) {

        String token = managerService.login(dto.getUsername(), dto.getPassword());
        return ApiResult.ok(Map.of("token", token));
    }

    @PostMapping("/logout")
    public ApiResult<String> logout(@RequestHeader("token") String token) {

        managerService.logout(token);
        return ApiResult.ok("退出登录成功");
    }

    @PostMapping("/create")
    public ApiResult<Manager> save(@RequestBody Manager manager) {
        System.out.println(manager);
        Manager created = managerService.createManager(manager);
        return ApiResult.ok(created);
    }


    @PostMapping("/getinfo")
    public ApiResult<Map<String, Object>> getInfo(@RequestHeader("token") String token) {
        // 根据 token 获取管理员详细信息（包含 role, menus, ruleNames）
        Map<String, Object> info = managerService.getInfoByToken(token);
        return ApiResult.ok(info);
    }

    @PostMapping("/updatepassword")
    public ApiResult<String> updatePassword(
            @RequestHeader("token") String token,
            @RequestBody UpdatePasswordDTO dto) {

        // 调用 Service 修改密码并注销 token
        managerService.updatePasswordByToken(
                token,
                dto.getOldPassword(),
                dto.getPassword(),
                dto.getRePassword()
        );

        return ApiResult.ok("修改成功");
    }


    @PostMapping("/{id}/update_status")
    public ApiResult<Boolean> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body,  // 接收 JSON
            @RequestHeader("token") String token
    ) {
        Integer status = (Integer) body.get("status");  // 从 JSON 里取
        boolean result = managerService.updateStatus(id, status);
        return ApiResult.ok(result);
    }

    @PostMapping("/manager/{id}/delete")
    public ApiResult<Boolean> deleteManager(
            @PathVariable Integer id,
            @RequestHeader("token") String token  // 暂时不校验
    ) {
        boolean result = managerService.deleteManager(id);
        return ApiResult.ok(result);
    }

    @PostMapping("/manager/{id}")
    public ApiResult<Boolean> updateManager(
            @RequestHeader("token") String token,
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

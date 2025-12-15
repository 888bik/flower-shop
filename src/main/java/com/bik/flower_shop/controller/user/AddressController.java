package com.bik.flower_shop.controller.user;// AddressController.java

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.AddressCreateDTO;
import com.bik.flower_shop.pojo.dto.AddressListVO;
import com.bik.flower_shop.pojo.dto.AddressUpdateDTO;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.pojo.vo.AddressVO;
import com.bik.flower_shop.service.TokenService;
import com.bik.flower_shop.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author bik
 */
@RestController
@RequestMapping("/user/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final UserAddressService addressService;
    private final TokenService tokenService;

    // 获取当前用户地址列表
    @GetMapping
    public ApiResult<AddressListVO> list(@RequestHeader("token") String token) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        AddressListVO result = addressService.listByUser(user.getId());
        return ApiResult.ok(result);
    }

    // 获取单个地址
    @GetMapping("/{id}")
    public ApiResult<AddressVO> get(@RequestHeader("token") String token, @PathVariable Integer id) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        AddressVO vo = addressService.getById(user.getId(), id);
        if (vo == null) {
            return ApiResult.fail("地址不存在");
        }
        return ApiResult.ok(vo);
    }

    // 新增地址
    @PostMapping
    public ApiResult<?> create(@RequestHeader("token") String token,
                               @Validated @RequestBody AddressCreateDTO dto) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        addressService.create(user.getId(), dto);
        return ApiResult.ok();
    }

    // 编辑地址
    @PutMapping("/{id}")
    public ApiResult<?> update(@RequestHeader("token") String token,
                               @PathVariable Integer id,
                               @Validated @RequestBody AddressUpdateDTO dto) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        addressService.update(user.getId(), id, dto);
        return ApiResult.ok();
    }

    // 删除地址
    @DeleteMapping("/{id}")
    public ApiResult<?> delete(@RequestHeader("token") String token, @PathVariable Integer id) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        addressService.delete(user.getId(), id);
        return ApiResult.ok();
    }

    // 设为默认
    @PostMapping("/{id}/default")
    public ApiResult<?> setDefault(@RequestHeader("token") String token, @PathVariable Integer id) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        addressService.setDefault(user.getId(), id);
        return ApiResult.ok();
    }
}

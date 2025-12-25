package com.bik.flower_shop.controller.user;// AddressController.java

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.context.BaseController;
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
public class AddressController extends BaseController {

    private final UserAddressService addressService;
    private final TokenService tokenService;

    @Override
    protected TokenService getTokenService() {
        return tokenService;
    }

    /**
     * 获取当前用户地址列表
     */
    @GetMapping
    public ApiResult<AddressListVO> getAddressList() {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        AddressListVO result = addressService.listByUser(user.getId());
        return ApiResult.ok(result);
    }


    /**
     * 获取单个地址
     */
    @GetMapping("/{id}")
    public ApiResult<AddressVO> getAddress(@PathVariable Integer id) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        AddressVO vo = addressService.getById(user.getId(), id);
        if (vo == null) {
            return ApiResult.fail("地址不存在");
        }
        return ApiResult.ok(vo);
    }

    /**
     * 新增地址
     */
    @PostMapping
    public ApiResult<?> createAddress(@Validated @RequestBody AddressCreateDTO dto) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        addressService.create(user.getId(), dto);
        return ApiResult.ok();
    }

    /**
     * 编辑地址
     */
    @PutMapping("/{id}")
    public ApiResult<?> updateAddress(@PathVariable Integer id,
                                      @Validated @RequestBody AddressUpdateDTO dto) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        addressService.update(user.getId(), id, dto);
        return ApiResult.ok();
    }


    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    public ApiResult<?> deleteAddress(@PathVariable Integer id) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        addressService.delete(user.getId(), id);
        return ApiResult.ok();
    }


    /**
     * 设为默认地址
     */
    @PostMapping("/{id}/default")
    public ApiResult<?> setDefault(@PathVariable Integer id) {
        User user = getCurrentUser();
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        addressService.setDefault(user.getId(), id);
        return ApiResult.ok();
    }
}

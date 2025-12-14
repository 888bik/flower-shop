package com.bik.flower_shop.controller.user;// package com.bik.flower_shop.controller.user;

import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.dto.AddCartDTO;
import com.bik.flower_shop.pojo.dto.UpdateCartNumDTO;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.pojo.vo.CartItemVO;
import com.bik.flower_shop.service.CartService;
import com.bik.flower_shop.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author bik
 */
@Slf4j
@RestController
@RequestMapping("/user/cart")
@RequiredArgsConstructor
public class CartController {

    private final TokenService tokenService;
    private final CartService cartService;

    /**
     * 添加购物车
     */
    @PostMapping("/add")
    public ApiResult<String> addToCart(@RequestHeader("token") String token,
                                       @RequestBody AddCartDTO dto) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        try {
            cartService.addToCart(user.getId(), dto);
            return ApiResult.ok("已加入购物车");
        } catch (IllegalArgumentException e) {
            return ApiResult.fail(e.getMessage());
        } catch (Exception e) {
            log.error("服务器错误", e);
            return ApiResult.fail("加入购物车失败");
        }
    }

    @GetMapping("/list")
    public ApiResult<List<CartItemVO>> listCart(@RequestHeader("token") String token) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        List<CartItemVO> list = cartService.listCartVO(user.getId());
        return ApiResult.ok(list);
    }

    @DeleteMapping("/remove/{id}")
    public ApiResult<String> remove(@RequestHeader("token") String token, @PathVariable("id") Integer id) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        cartService.removeFromCart(user.getId(), id);
        return ApiResult.ok("删除成功");
    }

    @PostMapping("/clear")
    public ApiResult<String> clear(@RequestHeader("token") String token) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        cartService.clearCart(user.getId());
        return ApiResult.ok("已清空购物车");
    }

    @PostMapping("/updateNum")
    public ApiResult<String> updateCartNum(@RequestHeader("token") String token, @RequestBody UpdateCartNumDTO dto) {
        User user = tokenService.getUserByToken(token);
        if (user == null) {
            return ApiResult.fail("未登录或 token 无效");
        }
        cartService.updateCartNum(user.getId(), dto);
        return ApiResult.ok("更新成功");
    }

}

package com.bik.flower_shop.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.exception.BusinessException;
import com.bik.flower_shop.mapper.UserMapper;
import com.bik.flower_shop.pojo.dto.RegisterDTO;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final TokenService tokenService;

    /**
     * 用户登录
     */
    public String login(String username, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || !PasswordUtil.verify(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        return tokenService.createToken(user, "user");
    }

    public void logout(String token) {
        tokenService.invalidateToken(token, "user");
    }

    /**
     * 用户注册
     */
    public String register(RegisterDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty() ||
                dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new BusinessException("用户名或密码不能为空");
        }

        if (!dto.getPassword().equals(dto.getRepassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        String username = dto.getUsername().trim();

        long count = userMapper.selectCount(
                new QueryWrapper<User>().eq("username", username)
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail().trim());
        }

        userMapper.insert(user);

        return tokenService.createToken(user, "user");
    }
}

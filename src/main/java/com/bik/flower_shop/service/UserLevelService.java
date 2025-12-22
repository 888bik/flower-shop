package com.bik.flower_shop.service;

import com.bik.flower_shop.mapper.UserLevelMapper;
import com.bik.flower_shop.pojo.dto.UpdateUserLevelDTO;
import com.bik.flower_shop.pojo.dto.UserLevelAddDTO;
import com.bik.flower_shop.pojo.dto.UserLevelDTO;
import com.bik.flower_shop.pojo.entity.UserLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class UserLevelService {

    private final UserLevelMapper userLevelMapper;

    public Map<String, Object> getLevelList(int page, int size, String keyword) {
        int offset = (page - 1) * size;
        List<UserLevel> levels = userLevelMapper.selectLevelList(offset, size, keyword);
        int totalCount = userLevelMapper.countLevelList(keyword);

        List<UserLevelDTO> dtoList = levels.stream().map(level -> {
            UserLevelDTO dto = new UserLevelDTO();
            dto.setId(level.getId());
            dto.setName(level.getName());
            dto.setLevel(level.getLevel());
            dto.setStatus(level.getStatus() != null && level.getStatus() ? 1 : 0);
            dto.setDiscount(level.getDiscount());
            dto.setMaxPrice(level.getMaxPrice());
            dto.setMaxTimes(level.getMaxTimes());
            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", dtoList);
        data.put("totalCount", totalCount);

        return data;
    }


    public void addLevel(UserLevelAddDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("等级名称不能为空");
        }
        if (dto.getLevel() == null || dto.getLevel() <= 0) {
            throw new IllegalArgumentException("等级权重必须大于0");
        }
        if (dto.getDiscount() == null || dto.getDiscount() <= 0 || dto.getDiscount() > 100) {
            throw new IllegalArgumentException("折扣必须在1~100之间");
        }

        UserLevel level = new UserLevel();
        level.setName(dto.getName().trim());
        level.setLevel(dto.getLevel());
        level.setStatus(dto.getStatus() != null && dto.getStatus());
        level.setDiscount(dto.getDiscount());
        level.setMaxPrice(dto.getMaxPrice() != null ? dto.getMaxPrice() : 0);
        level.setMaxTimes(dto.getMaxTimes() != null ? dto.getMaxTimes() : 0);

        userLevelMapper.insertUserLevel(level);
    }


    public void updateLevel(Integer id, UpdateUserLevelDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("等级ID不能为空");
        }

        UserLevel level = new UserLevel();
        level.setId(id);
        level.setName(dto.getName().trim());
        level.setLevel(dto.getLevel());
        level.setStatus(dto.getStatus() != null && dto.getStatus());
        level.setDiscount(dto.getDiscount());
        level.setMaxPrice(dto.getMaxPrice() != null ? dto.getMaxPrice() : 0);
        level.setMaxTimes(dto.getMaxTimes() != null ? dto.getMaxTimes() : 0);

        userLevelMapper.updateUserLevel(level);
    }

    public void deleteLevel(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("等级ID不能为空");
        }
        userLevelMapper.deleteUserLevel(id);
    }

    public void updateUserLevelStatus(Integer id, Byte status) {
        userLevelMapper.updateUserLevelStatus(id, status);
    }

}

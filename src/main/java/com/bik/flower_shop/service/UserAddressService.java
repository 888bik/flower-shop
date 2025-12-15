package com.bik.flower_shop.service;// UserAddressService.java

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bik.flower_shop.mapper.UserAddressesMapper;
import com.bik.flower_shop.pojo.dto.AddressCreateDTO;
import com.bik.flower_shop.pojo.dto.AddressListVO;
import com.bik.flower_shop.pojo.dto.AddressUpdateDTO;
import com.bik.flower_shop.pojo.entity.UserAddresses;
import com.bik.flower_shop.pojo.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressesMapper mapper;

    public AddressListVO listByUser(Integer userId) {
        List<UserAddresses> list = mapper.selectList(
                new LambdaQueryWrapper<UserAddresses>().eq(UserAddresses::getUserId, userId)
                        .orderByDesc(UserAddresses::getIsDefault).orderByDesc(UserAddresses::getLastUsedTime)
        );
        List<AddressVO> voList = list.stream().map(this::toVO).collect(Collectors.toList());

        AddressListVO result = new AddressListVO();
        result.setList(voList);
        result.setTotalCount(voList.size());

        return result;
    }

    public AddressVO getById(Integer userId, Integer id) {
        UserAddresses a = mapper.selectOne(new LambdaQueryWrapper<UserAddresses>()
                .eq(UserAddresses::getUserId, userId).eq(UserAddresses::getId, id));
        return a == null ? null : toVO(a);
    }

    @Transactional
    public void create(Integer userId, AddressCreateDTO dto) {
        UserAddresses e = new UserAddresses();
        e.setUserId(userId);
        e.setProvince(dto.getProvince());
        e.setCity(dto.getCity());
        e.setDistrict(dto.getDistrict());
        e.setAddress(dto.getAddress());
        e.setZip(dto.getZip());
        e.setName(dto.getName());
        e.setPhone(dto.getPhone());
        boolean isDefault = Boolean.TRUE.equals(dto.getIsDefault());
        e.setIsDefault(isDefault ? (byte) 1 : (byte) 0);
        int now = (int) Instant.now().getEpochSecond();
        e.setCreateTime(now);
        e.setUpdateTime(now);

        // 如果设为默认，先清空其他默认地址
        if (isDefault) {
            mapper.update(
                    null,
                    new UpdateWrapper<UserAddresses>()
                            .lambda()
                            .eq(UserAddresses::getUserId, userId)
                            .set(UserAddresses::getIsDefault, 0)
            );
        }
        mapper.insert(e);
    }

    @Transactional
    public void update(Integer userId, Integer addressId, AddressUpdateDTO dto) {
        UserAddresses exist = mapper.selectById(addressId);
        if (exist == null || !exist.getUserId().equals(userId)) {
            throw new IllegalArgumentException("地址不存在或无权限");
        }
        exist.setProvince(dto.getProvince());
        exist.setCity(dto.getCity());
        exist.setDistrict(dto.getDistrict());
        exist.setAddress(dto.getAddress());
        exist.setZip(dto.getZip());
        exist.setName(dto.getName());
        exist.setPhone(dto.getPhone());
        exist.setUpdateTime((int) Instant.now().getEpochSecond());
        if (dto.getIsDefault() != null) {
            exist.setIsDefault(dto.getIsDefault() ? (byte) 1 : (byte) 0);
        }

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            mapper.update(
                    null,
                    new UpdateWrapper<UserAddresses>()
                            .lambda()
                            .eq(UserAddresses::getUserId, userId)
                            .set(UserAddresses::getIsDefault, 0)
            );
        }
        mapper.updateById(exist);
    }

    public void delete(Integer userId, Integer id) {
        UserAddresses exist = mapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            throw new IllegalArgumentException("地址不存在或无权限");
        }
        mapper.deleteById(id);
    }

    @Transactional
    public void setDefault(Integer userId, Integer id) {
        UserAddresses exist = mapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            throw new IllegalArgumentException("地址不存在或无权限");
        }
        // 清空其他默认
        mapper.update(null, new UpdateWrapper<UserAddresses>()
                .lambda().eq(UserAddresses::getUserId, userId).set(UserAddresses::getIsDefault, 0));
        // 设置当前为默认
        exist.setIsDefault((byte) 1);
        exist.setUpdateTime((int) Instant.now().getEpochSecond());
        mapper.updateById(exist);
    }

    private AddressVO toVO(UserAddresses e) {
        AddressVO v = new AddressVO();
        // copy fields
        v.setId(e.getId());
        v.setUserId(e.getUserId());
        v.setProvince(e.getProvince());
        v.setCity(e.getCity());
        v.setDistrict(e.getDistrict());
        v.setAddress(e.getAddress());
        v.setZip(e.getZip());
        v.setName(e.getName());
        v.setPhone(e.getPhone());
        v.setIsDefault(e.getIsDefault());
        v.setLastUsedTime(e.getLastUsedTime());
        v.setCreateTime(e.getCreateTime());
        v.setUpdateTime(e.getUpdateTime());
        return v;
    }
}

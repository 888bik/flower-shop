package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author bik
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

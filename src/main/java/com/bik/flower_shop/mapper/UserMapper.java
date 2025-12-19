package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.User;
import com.bik.flower_shop.pojo.vo.UserSimpleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author bik
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT id, username, nickname, avatar FROM user WHERE id = #{id}")
    UserSimpleVO selectSimpleById(Integer id);
}

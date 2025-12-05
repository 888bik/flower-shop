package com.bik.flower_shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bik.flower_shop.pojo.entity.Rule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author bik
 */
@Mapper
public interface RuleMapper extends BaseMapper<Rule> {
    @Select("SELECT * FROM rule ORDER BY `order` ")
    List<Rule> selectAllRules();
}

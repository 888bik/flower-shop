package com.bik.flower_shop.service;

import com.bik.flower_shop.mapper.ExpressCompanyMapper;
import com.bik.flower_shop.pojo.entity.ExpressCompany;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bik
 */
@RequiredArgsConstructor
@Service
public class ExpressService {
    private final ExpressCompanyMapper expressCompanyMapper;

    public List<ExpressCompany> listAllCompanies() {
        // 按 order 排序
        return expressCompanyMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExpressCompany>()
                        .orderByDesc(ExpressCompany::getOrder)
        );
    }
}

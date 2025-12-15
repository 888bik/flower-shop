package com.bik.flower_shop.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.mapper.SysAreaMapper;
import com.bik.flower_shop.mapper.SysCityMapper;
import com.bik.flower_shop.mapper.SysDistrictMapper;
import com.bik.flower_shop.mapper.SysProvinceMapper;
import com.bik.flower_shop.pojo.entity.SysArea;
import com.bik.flower_shop.pojo.entity.SysCity;
import com.bik.flower_shop.pojo.entity.SysDistrict;
import com.bik.flower_shop.pojo.entity.SysProvince;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author bik
 */
@RestController
@RequestMapping("/user/areas")
@RequiredArgsConstructor
public class AreaController {
    private final SysProvinceMapper provinceMapper;
    private final SysCityMapper cityMapper;
    private final SysDistrictMapper districtMapper;
    private final SysAreaMapper areaMapper;


    @GetMapping
    public ApiResult<List<SysArea>> areas() {
        return ApiResult.ok(areaMapper.selectList(null));
    }

    @GetMapping("/provinces")
    public ApiResult<List<SysProvince>> provinces(@RequestParam(required = false) Integer areaId) {
        LambdaQueryWrapper<SysProvince> qw = new LambdaQueryWrapper<>();
        if (areaId != null) {
            qw.eq(SysProvince::getSysAreaId, areaId);
        }
        return ApiResult.ok(provinceMapper.selectList(qw));
    }

    @GetMapping("/cities")
    public ApiResult<List<SysCity>> cities(@RequestParam Integer provinceId) {
        return ApiResult.ok(cityMapper.selectList(new LambdaQueryWrapper<SysCity>().eq(SysCity::getSysProvinceId, provinceId)));
    }

    @GetMapping("/districts")
    public ApiResult<List<SysDistrict>> districts(@RequestParam Integer cityId) {
        return ApiResult.ok(districtMapper.selectList(new LambdaQueryWrapper<SysDistrict>().eq(SysDistrict::getSysCityId, cityId)));
    }
}

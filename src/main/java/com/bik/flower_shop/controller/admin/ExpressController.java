package com.bik.flower_shop.controller.admin;

import com.bik.flower_shop.annotation.AuthRequired;
import com.bik.flower_shop.common.ApiResult;
import com.bik.flower_shop.pojo.entity.ExpressCompany;
import com.bik.flower_shop.service.ExpressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author bik
 */
@RestController
@RequestMapping("/admin/express")
@RequiredArgsConstructor
@AuthRequired(role = "admin")
public class ExpressController {

    private final ExpressService expressService;

    @GetMapping("/companies")
    public ApiResult<List<ExpressCompany>> listCompanies() {
        List<ExpressCompany> list = expressService.listAllCompanies();
        return ApiResult.ok(list);
    }
}

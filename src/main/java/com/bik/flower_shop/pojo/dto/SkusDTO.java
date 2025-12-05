package com.bik.flower_shop.pojo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接收表单（application/x-www-form-urlencoded）
 * 注意：前端字段名为 "default"，但 Java 关键字不能用作字段名，
 * 所以 DTO 使用 defaultValues 并在 Controller 中手动赋值。
 * @author bik
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkusDTO {

    @NotNull(message = "status 不能为空")
    @Min(value = 0, message = "status 必须是非负整数")
    private Integer status;

    @NotBlank(message = "name 不能为空")
    private String name;

    @NotNull(message = "order 不能为空")
    @Min(value = 0, message = "order 必须是非负整数")
    private Integer order;

    /**
     * 对应前端表单的字段名 "defaults"
     */
    @NotBlank(message = "defaults 不能为空")
    private String defaults;
}

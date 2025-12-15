package com.bik.flower_shop.pojo.dto;

import com.bik.flower_shop.pojo.vo.AddressVO;
import lombok.Data;
import java.util.List;

/**
 * @author bik
 */
@Data
public class AddressListVO {
    private Integer totalCount;
    private List<AddressVO> list;
}

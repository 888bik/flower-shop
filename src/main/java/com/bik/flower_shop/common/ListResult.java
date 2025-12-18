package com.bik.flower_shop.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author bik
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListResult<T> {
    private List<T> list;
    private long totalCount;
}

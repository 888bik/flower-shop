package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bik.flower_shop.mapper.SkusMapper;
import com.bik.flower_shop.pojo.entity.Skus;
import com.bik.flower_shop.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class SkusService extends ServiceImpl<SkusMapper, Skus> {

    private final SkusMapper skusMapper;

    /**
     * 创建规格
     *
     * @param statusInt 状态（1/0）
     * @param name      规格名称
     * @param order     排序
     * @param defaults  规格值，逗号分隔
     * @return 插入后的 Skus（含 id）
     */
    @Transactional(rollbackFor = Exception.class)
    public Skus createSkus(Integer statusInt, String name, Integer order, String defaults) {
        Skus skus = new Skus();
        skus.setName(name);
        skus.setOrder(order);

        skus.setStatus(statusInt != null && statusInt == 1);
        skus.setDefaults(defaults);

        int now = (int) Instant.now().getEpochSecond();
        skus.setCreateTime(now);
        skus.setUpdateTime(now);

        skusMapper.insert(skus);
        return skus;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateSkus(Integer id, Integer statusInt, String name, Integer order, String defaults) {
        Skus skus = skusMapper.selectById(id);
        if (skus == null) {
            return false;
        }

        skus.setName(name);
        skus.setOrder(order);
        skus.setStatus(statusInt != null && statusInt == 1);
        skus.setDefaults(defaults);
        skus.setUpdateTime((int) Instant.now().getEpochSecond());

        return skusMapper.updateById(skus) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateSkusStatus(Integer id, Integer statusInt) {
        Skus skus = skusMapper.selectById(id);
        if (skus == null) {
            return false;
        }

        skus.setStatus(statusInt != null && statusInt == 1);
        skus.setUpdateTime((int) Instant.now().getEpochSecond());

        return skusMapper.updateById(skus) > 0;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getListSkus(int page, int pageSize) {
        int offset = (page - 1) * pageSize;

        List<Skus> list = skusMapper.selectPage(offset, pageSize);
        int totalCount = skusMapper.countAll();

        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Skus s : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", s.getId());
            item.put("name", s.getName());
            item.put("type", s.getType());
            item.put("create_time", TimeUtils.format(s.getCreateTime()));
            item.put("update_time", TimeUtils.format(s.getUpdateTime()));
            item.put("status", s.getStatus() ? 1 : 0);
            item.put("order", s.getOrder());
            item.put("default", s.getDefaults());
            mapped.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", mapped);
        data.put("totalCount", totalCount);

        return data;
    }

    public int deleteAllSkus(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        boolean removed = this.removeByIds(ids);
        return removed ? ids.size() : 0;
    }

}

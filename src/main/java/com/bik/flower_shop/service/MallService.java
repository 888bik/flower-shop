package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.mapper.CategoryMapper;
import com.bik.flower_shop.mapper.GoodsBannerMapper;
import com.bik.flower_shop.mapper.GoodsCategoryMapper;
import com.bik.flower_shop.mapper.GoodsMapper;
import com.bik.flower_shop.pojo.dto.MallQueryDTO;
import com.bik.flower_shop.pojo.entity.Category;
import com.bik.flower_shop.pojo.entity.Goods;
import com.bik.flower_shop.pojo.entity.GoodsBanner;
import com.bik.flower_shop.pojo.entity.GoodsCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MallService {

    private final GoodsMapper goodsMapper;
    private final GoodsBannerMapper goodsBannerMapper;
    private final GoodsCategoryMapper goodsCategoryMapper;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> listMallGoods(MallQueryDTO dto) {
        int page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int limit = dto.getLimit() == null || dto.getLimit() < 1 ? 12 : dto.getLimit();

        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        // 只获取在售、未删除的商品（商城通常只展示 status = 1）
        wrapper.eq("status", 1).isNull("delete_time");

        if (dto.getCategoryId() != null) {
            // 通过关联表查询商品 id 集合，再用 wrapper.in("id", ids)
            List<Integer> goodsIds = Optional.ofNullable(
                    goodsCategoryMapper.selectGoodsIdsByCategoryId(dto.getCategoryId())
            ).orElse(Collections.emptyList());

            if (goodsIds.isEmpty()) {
                return Map.of("list", Collections.emptyList(), "page", page, "pageSize", limit, "totalCount", 0L);
            }
            wrapper.in("id", goodsIds);
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            wrapper.like("title", dto.getTitle().trim());
        }

        // 只选择需要的列（极重要：减少 DB -> 应用的列数据）
        wrapper.select("id", "title", "cover", "min_price", "min_oprice", "rating", "sale_count", "stock", "unit", "sku_type");

        wrapper.orderByDesc("id");

        Page<Map<String, Object>> pageObj = new Page<>(page, limit);
        // 使用 selectMaps 分页返回 Map（避免加载整个实体）
        IPage<Map<String, Object>> pageResult = goodsMapper.selectMapsPage(pageObj, wrapper);
        List<Map<String, Object>> records = Optional.ofNullable(pageResult.getRecords()).orElse(Collections.emptyList());

        // 批量收集 goodsId
        List<Integer> ids = records.stream()
                .map(m -> (Integer) m.get("id"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 批量查询 banners（只需要 url）
        List<GoodsBanner> banners = ids.isEmpty() ? Collections.emptyList()
                : goodsBannerMapper.selectList(new QueryWrapper<GoodsBanner>().in("goods_id", ids)
                .select("goods_id", "url"));

        Map<Integer, List<String>> bannerMap = banners.stream()
                .collect(Collectors.groupingBy(GoodsBanner::getGoodsId,
                        Collectors.mapping(GoodsBanner::getUrl, Collectors.toList())));

        // 批量查询 goods->categories（通过 goods_category 关联表）
        List<GoodsCategory> goodsCats = ids.isEmpty() ? Collections.emptyList()
                : goodsCategoryMapper.selectByGoodsIds(ids); // 需要实现返回 goodsId, categoryId

        // 去重 categoryIds 并查询 name
        Set<Integer> catIds = goodsCats.stream().map(GoodsCategory::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, Category> catMap = catIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(new ArrayList<>(catIds)).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // goodsId -> List<CategoryVO>
        Map<Integer, List<Map<String, Object>>> goodsToCats = goodsCats.stream()
                .collect(Collectors.groupingBy(GoodsCategory::getGoodsId,
                        Collectors.mapping(gc -> {
                            Category c = catMap.get(gc.getCategoryId());
                            if (c == null) return null;
                            Map<String, Object> m = new HashMap<>();
                            m.put("id", c.getId());
                            m.put("name", c.getName());
                            return m;
                        }, Collectors.toList())));

        // 构造返回 list（只保留商城需要字段）
        List<Map<String, Object>> mallList = new ArrayList<>();
        for (Map<String, Object> row : records) {
            Integer gid = (Integer) row.get("id");
            Map<String, Object> item = new HashMap<>();
            item.put("id", gid);
            item.put("title", row.get("title"));
            item.put("cover", row.get("cover"));
            // 保证价格字符串格式化（保持前端一致）
            item.put("minPrice", row.get("min_price") != null ? row.get("min_price").toString() : "0.00");
            item.put("minOprice", row.get("min_oprice") != null ? row.get("min_oprice").toString() : "0.00");
            item.put("rating", row.get("rating"));
            item.put("saleCount", row.get("sale_count"));
            item.put("stock", row.get("stock"));
            item.put("unit", row.get("unit"));
            item.put("skuType", row.get("sku_type"));
            item.put("banners", bannerMap.getOrDefault(gid, Collections.emptyList()));
            item.put("categories", goodsToCats.getOrDefault(gid, Collections.emptyList()));
            mallList.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", mallList);
        data.put("page", page);
        data.put("pageSize", limit);
        data.put("totalCount", pageResult.getTotal());
        return data;
    }

}

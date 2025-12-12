package com.bik.flower_shop.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.mapper.*;
import com.bik.flower_shop.pojo.dto.*;
import com.bik.flower_shop.pojo.entity.*;
import com.bik.flower_shop.pojo.vo.GoodsSkusCardVO;
import com.bik.flower_shop.pojo.vo.GoodsSkusVO;
import com.bik.flower_shop.pojo.vo.GoodsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@RequiredArgsConstructor
@Service
public class GoodsService {

    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;
    private final GoodsBannerMapper goodsBannerMapper;
    private final GoodsAttrsMapper goodsAttrsMapper;
    private final GoodsSkusMapper goodsSkusMapper;
    private final GoodsSkusCardMapper goodsSkusCardMapper;
    private final GoodsSkusCardValueMapper goodsSkusCardValueMapper;
    private final GoodsCategoryMapper goodsCategoryMapper;


    public void checkGoods(Integer id, Byte isCheck) {
        Integer status = (isCheck != null && isCheck == 1) ? 1 : 0;
        System.out.println("isCheck: " + isCheck);

        int rows = goodsMapper.update(null,
                new UpdateWrapper<Goods>()
                        .eq("id", id)
                        .set("ischeck", isCheck)
                        .set("status", status)
        );

        if (rows == 0) {
            throw new RuntimeException("商品不存在或更新失败");
        }
    }

    public void updateContent(Integer id, String content) {

        UpdateWrapper<Goods> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .set("content", content);

        int rows = goodsMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new RuntimeException("商品不存在或更新失败");
        }
    }


    /**
     * 设置商品轮播图
     */
    public List<GoodsBanner> setGoodsBanners(Integer goodsId, List<String> urls) {
        if (goodsId == null) {
            return List.of();
        }

        // 1. 删除原有轮播图
        goodsBannerMapper.deleteByGoodsId(goodsId);

        // 2. 批量新增
        List<GoodsBanner> list = new ArrayList<>();
        long now = Instant.now().getEpochSecond();
        if (urls != null) {
            for (String url : urls) {
                if (url == null || url.isBlank()) {
                    continue;
                }
                GoodsBanner banner = new GoodsBanner();
                banner.setGoodsId(goodsId);
                banner.setUrl(url);
                banner.setCreateTime(now);
                banner.setUpdateTime(now);
                goodsBannerMapper.insert(banner);
                list.add(banner);
            }
        }

        return list;
    }

    public GoodsVO getGoodsById(Integer id) {
        if (id == null) {
            return null;
        }
        Goods g = goodsMapper.selectById(id);
        if (g == null) {
            return null;
        }

        GoodsVO vo = new GoodsVO();
        BeanUtils.copyProperties(g, vo);

        // 转换金额字段为字符串
        vo.setMinPrice(safeDecimalToString(g.getMinPrice()));
        vo.setMinOprice(safeDecimalToString(g.getMinOprice()));

        // skuValue 解析（保持你已有逻辑）
        if (g.getSkuValue() != null && !g.getSkuValue().isBlank()) {
            try {
                Map<String, Object> skuMap = JSON.parseObject(g.getSkuValue(), Map.class);
                vo.setSkuValue(skuMap);
            } catch (Exception e) {
                vo.setSkuValue(g.getSkuValue());
            }
        } else {
            vo.setSkuValue(null);
        }

        // 分类
        if (g.getCategoryId() != null) {
            vo.setCategory(categoryMapper.selectById(g.getCategoryId()));
        }

        // Banner / Attrs
        vo.setGoodsBanner(Optional.ofNullable(
                goodsBannerMapper.selectList(new QueryWrapper<GoodsBanner>().eq("goods_id", id))
        ).orElse(Collections.emptyList()));

        vo.setGoodsAttrs(Optional.ofNullable(
                goodsAttrsMapper.selectList(new QueryWrapper<GoodsAttrs>().eq("goods_id", id))
        ).orElse(Collections.emptyList()));

        // 关联 Skus（并尝试把 skus 字符串解析到 skusObj）
        List<GoodsSkus> skus = Optional.ofNullable(
                goodsSkusMapper.selectList(new QueryWrapper<GoodsSkus>().eq("goods_id", id))
        ).orElse(Collections.emptyList());

        List<GoodsSkusVO> skusVo = skus.stream().map(s -> {
            GoodsSkusVO sv = new GoodsSkusVO();
            BeanUtils.copyProperties(s, sv); // 复制基本字段
            String raw = s.getSkus();
            if (raw != null && !raw.isBlank()) {
                try {
                    // 解析成 List<Map<String,Object>>
                    List<Map<String, Object>> parsed = JSON.parseObject(
                            raw,
                            new TypeReference<List<Map<String, Object>>>() {
                            }
                    );
                    sv.setSkus(parsed);
                } catch (Exception ex) {
                    // 解析失败时置空或保留 null，避免抛异常
                    sv.setSkus(null);
                }
            } else {
                sv.setSkus(null);
            }
            return sv;
        }).collect(Collectors.toList());

        vo.setGoodsSkus(skusVo);

        // 关联 SkusCard + Values（按原有逻辑）
        List<GoodsSkusCard> cards = Optional.ofNullable(
                goodsSkusCardMapper.selectList(new QueryWrapper<GoodsSkusCard>().eq("goods_id", id))
        ).orElse(Collections.emptyList());

        if (!cards.isEmpty()) {
            List<Integer> cardIds = cards.stream().map(GoodsSkusCard::getId).collect(Collectors.toList());
            List<GoodsSkusCardValue> values = Optional.ofNullable(
                    goodsSkusCardValueMapper.selectList(new QueryWrapper<GoodsSkusCardValue>().in("goods_skus_card_id", cardIds))
            ).orElse(Collections.emptyList());

            Map<Integer, List<GoodsSkusCardValue>> valueMap = values.stream()
                    .collect(Collectors.groupingBy(GoodsSkusCardValue::getGoodsSkusCardId));

            List<GoodsSkusCardVO> voCards = new ArrayList<>();
            for (GoodsSkusCard card : cards) {
                GoodsSkusCardVO cv = new GoodsSkusCardVO();
                BeanUtils.copyProperties(card, cv);
                cv.setGoodsSkusCardValue(valueMap.getOrDefault(card.getId(), Collections.emptyList()));
                voCards.add(cv);
            }
            vo.setGoodsSkusCard(voCards);
        } else {
            vo.setGoodsSkusCard(Collections.emptyList());
        }

        return vo;
    }


    @Transactional
    public int changeGoodsStatus(ChangeGoodsStatusDTO dto) {
        List<Integer> ids = dto.getIds();
        Byte status = dto.getStatus();
        if (ids == null || ids.isEmpty() || status == null) {
            return 0;
        }

        int updatedCount = 0;
        for (Integer id : ids) {
            Goods goods = goodsMapper.selectById(id);
            if (goods != null) {
                goods.setStatus(status);
                goods.setUpdateTime(Instant.now().getEpochSecond());
                updatedCount += goodsMapper.updateById(goods);
            }
        }
        return updatedCount;
    }


    @Transactional
    public boolean updateGoods(Integer id, UpdateGoodsDTO dto) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            return false;
        }

        goods.setTitle(dto.getTitle());
        goods.setCover(dto.getCover());
        goods.setDescription(dto.getDescription());
        goods.setUnit(dto.getUnit());
        goods.setStock(dto.getStock());
        goods.setMinStock(dto.getMinStock());
        goods.setStatus(dto.getStatus());
        goods.setStockDisplay(dto.getStockDisplay());
        goods.setMinPrice(dto.getMinPrice());
        goods.setMinOprice(dto.getMinOprice());

        // 兼容单一字段
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            goods.setCategoryId(dto.getCategoryIds().get(0));
        } else if (dto.getCategoryId() != null) {
            goods.setCategoryId(dto.getCategoryId());
        }

        goods.setUpdateTime(Instant.now().getEpochSecond());
        goodsMapper.updateById(goods);

        // 更新关联表
        goodsCategoryMapper.deleteByGoodsId(id);
        List<Integer> categoryIds = dto.getCategoryIds();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            LinkedHashSet<Integer> set = new LinkedHashSet<>(categoryIds);
            List<GoodsCategory> list = set.stream().map(cid -> {
                GoodsCategory gc = new GoodsCategory();
                gc.setGoodsId(id);
                gc.setCategoryId(cid);
                return gc;
            }).collect(Collectors.toList());
            goodsCategoryMapper.batchInsert(list);
        }

        return true;
    }


    @Transactional
    public void softDeleteGoods(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        UpdateWrapper<Goods> wrapper = new UpdateWrapper<>();
        wrapper.in("id", ids).set("delete_time", System.currentTimeMillis() / 1000);

        goodsMapper.update(null, wrapper);
    }

    @Transactional
    public void restoreGoods(List<Integer> ids) {
        UpdateWrapper<Goods> wrapper = new UpdateWrapper<>();
        wrapper.in("id", ids).set("delete_time", null);

        goodsMapper.update(null, wrapper);
    }


    @Transactional
    public void deleteForceGoods(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 使用 Wrapper 条件删除
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);
        goodsMapper.delete(wrapper);
    }

    /**
     * 保存商品
     */
    @Transactional
    public Goods saveGoods(Goods goods) {
        if (goods.getIscheck() == null) {
            goods.setIscheck((byte) 0);
        }

        long now = Instant.now().getEpochSecond();
        goods.setCreateTime(now);
        goods.setUpdateTime(now);

        // 兼容旧字段，如果 categoryId 为空，则用 categoryIds 的第一个
        List<Integer> categoryIds = goods.getCategoryIds();
        if ((goods.getCategoryId() == null || goods.getCategoryId() == 0) && categoryIds != null && !categoryIds.isEmpty()) {
            goods.setCategoryId(categoryIds.get(0));
        }

        // 插入商品主表
        goodsMapper.insert(goods);

        // 保存关联表（去重）
        if (categoryIds != null && !categoryIds.isEmpty()) {
            LinkedHashSet<Integer> set = new LinkedHashSet<>(categoryIds);
            List<GoodsCategory> list = set.stream().map(cid -> {
                GoodsCategory gc = new GoodsCategory();
                gc.setGoodsId(goods.getId());
                gc.setCategoryId(cid);
                return gc;
            }).collect(Collectors.toList());

            if (!list.isEmpty()) {
                goodsCategoryMapper.batchInsert(list);
            }
        }
        return goods;
    }


    @Transactional(readOnly = true)
    public Map<String, Object> listGoods(GoodsQueryDTO dto) {
        if (dto == null) {
            dto = new GoodsQueryDTO();
        }

        int page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int limit = dto.getLimit() == null || dto.getLimit() < 1 ? 10 : dto.getLimit();

        // --- 1. 多分类筛选（通过 goods_category 关联表） ---
        List<Integer> filterGoodsIds = null;
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            // 假设 goodsCategoryMapper.selectGoodsIdsByCategoryIds 返回 List<Integer>
            filterGoodsIds = Optional.ofNullable(
                    goodsCategoryMapper.selectGoodsIdsByCategoryIds(dto.getCategoryIds())
            ).orElse(Collections.emptyList());

            // 如果没有任何商品匹配这些分类，直接返回空结果（性能优先）
            if (filterGoodsIds.isEmpty()) {
                List<Category> cates = Optional.ofNullable(categoryMapper.selectList(null)).orElse(Collections.emptyList());
                Map<String, Object> emptyRes = new HashMap<>();
                emptyRes.put("list", Collections.emptyList());
                emptyRes.put("page", page);
                emptyRes.put("pageSize", limit);
                emptyRes.put("totalCount", 0L);
                emptyRes.put("cates", cates);
                return emptyRes;
            }
        }

        // --- 2. 构造查询条件 ---
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();

        // 分类过滤（优先使用 filterGoodsIds）
        if (filterGoodsIds != null) {
            wrapper.in("id", filterGoodsIds);
        } else if (dto.getCategoryId() != null) {
            wrapper.eq("category_id", dto.getCategoryId());
        }

        // 删除状态筛选
        if ("delete".equals(dto.getTab())) {
            wrapper.isNotNull("delete_time");
        } else {
            wrapper.isNull("delete_time");
        }

        // 其他 tab 筛选
        if (dto.getTab() != null) {
            switch (dto.getTab()) {
                case "checking" -> wrapper.eq("ischeck", 0);
                case "saling" -> wrapper.eq("status", 1);
                case "off" -> wrapper.eq("status", 0);
                case "min_stock" -> wrapper.apply("stock <= min_stock");
            }
        }

        // 标题搜索
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            wrapper.like("title", dto.getTitle().trim());
        }

        wrapper.orderByDesc("id");

        // --- 3. 显式总数计数（便于调试 & 保证准确） ---
        long total = goodsMapper.selectCount(wrapper).longValue();

        // 如果 total == 0，快速返回
        if (total == 0) {
            List<Category> cates = Optional.ofNullable(categoryMapper.selectList(null)).orElse(Collections.emptyList());
            return Map.of(
                    "list", Collections.emptyList(),
                    "page", page,
                    "pageSize", limit,
                    "totalCount", 0L,
                    "cates", cates
            );
        }

        // --- 4. 分页查询主数据 ---
        Page<Goods> pageObj = new Page<>(page, limit);
        Page<Goods> goodsPage = goodsMapper.selectPage(pageObj, wrapper);
        List<Goods> goodsList = Optional.ofNullable(goodsPage.getRecords()).orElse(Collections.emptyList());

        // --- 5. 批量查询所有关联数据（一次性） ---
        List<Integer> goodsIds = goodsList.stream().map(Goods::getId).collect(Collectors.toList());

        List<GoodsBanner> banners = Optional.ofNullable(
                goodsBannerMapper.selectList(new QueryWrapper<GoodsBanner>().in("goods_id", goodsIds))
        ).orElse(Collections.emptyList());

        List<GoodsAttrs> attrs = Optional.ofNullable(
                goodsAttrsMapper.selectList(new QueryWrapper<GoodsAttrs>().in("goods_id", goodsIds))
        ).orElse(Collections.emptyList());

        List<GoodsSkus> skus = Optional.ofNullable(
                goodsSkusMapper.selectList(new QueryWrapper<GoodsSkus>().in("goods_id", goodsIds))
        ).orElse(Collections.emptyList());

        List<GoodsSkusCard> cards = Optional.ofNullable(
                goodsSkusCardMapper.selectList(new QueryWrapper<GoodsSkusCard>().in("goods_id", goodsIds))
        ).orElse(Collections.emptyList());

        List<GoodsSkusCardValue> cardValues = Collections.emptyList();
        if (!cards.isEmpty()) {
            List<Integer> cardIds = cards.stream().map(GoodsSkusCard::getId).filter(Objects::nonNull).collect(Collectors.toList());
            cardValues = Optional.ofNullable(
                    goodsSkusCardValueMapper.selectList(new QueryWrapper<GoodsSkusCardValue>().in("goods_skus_card_id", cardIds))
            ).orElse(Collections.emptyList());
        }

        // 如果你用了 goods_category 关联表，则获取这些商品对应的 category 关联关系
        List<GoodsCategory> goodsCats = Optional.ofNullable(
                goodsCategoryMapper.selectByGoodsIds(goodsIds)
        ).orElse(Collections.emptyList());

        // --- 6. 构造映射以便高效合并 ---
        Map<Integer, List<GoodsBanner>> bannerMap = groupBy(banners, GoodsBanner::getGoodsId);
        Map<Integer, List<GoodsAttrs>> attrsMap = groupBy(attrs, GoodsAttrs::getGoodsId);

        Map<Integer, List<GoodsSkusVO>> skusMap = skus.stream().map(s -> {
            GoodsSkusVO vo = new GoodsSkusVO();
            BeanUtils.copyProperties(s, vo);
            if (s.getSkus() != null && !s.getSkus().isBlank()) {
                try {
                    vo.setSkus(JSON.parseObject(s.getSkus(), new com.alibaba.fastjson.TypeReference<List<Map<String, Object>>>() {
                    }));
                } catch (Exception ignore) {
                }
            }
            return vo;
        }).collect(Collectors.groupingBy(GoodsSkusVO::getGoodsId));

        Map<Integer, List<GoodsSkusCard>> cardsMap = groupBy(cards, GoodsSkusCard::getGoodsId);
        Map<Integer, List<GoodsSkusCardValue>> cardValuesMap = groupBy(cardValues, GoodsSkusCardValue::getGoodsSkusCardId);

        // goods -> [categoryId,...]
        Map<Integer, List<Integer>> goodsToCatIds = goodsCats.stream()
                .collect(Collectors.groupingBy(GoodsCategory::getGoodsId,
                        Collectors.mapping(GoodsCategory::getCategoryId, Collectors.toList())));

        // 批量查询所有被引用的 category（去重）
        Set<Integer> allCatIds = goodsCats.stream()
                .map(GoodsCategory::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Integer, Category> catMap = allCatIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(new ArrayList<>(allCatIds)).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));

        // 同时给前端返回所有一级分类（兼容你现有）
        List<Category> cates = Optional.ofNullable(categoryMapper.selectList(null)).orElse(Collections.emptyList());

        // --- 7. 构造返回 VO 列表 ---
        List<GoodsVO> result = new ArrayList<>();
        for (Goods g : goodsList) {
            GoodsVO vo = new GoodsVO();
            BeanUtils.copyProperties(g, vo);

            vo.setMinPrice(safeDecimalToString(g.getMinPrice()));
            vo.setMinOprice(safeDecimalToString(g.getMinOprice()));
            vo.setGoodsBanner(bannerMap.getOrDefault(g.getId(), List.of()));
            vo.setGoodsAttrs(attrsMap.getOrDefault(g.getId(), List.of()));
            vo.setGoodsSkus(skusMap.getOrDefault(g.getId(), List.of()));

            // skus card + values
            List<GoodsSkusCardVO> voCards = new ArrayList<>();
            for (GoodsSkusCard card : cardsMap.getOrDefault(g.getId(), List.of())) {
                GoodsSkusCardVO cv = new GoodsSkusCardVO();
                BeanUtils.copyProperties(card, cv);
                cv.setGoodsSkusCardValue(cardValuesMap.getOrDefault(card.getId(), List.of()));
                voCards.add(cv);
            }
            vo.setGoodsSkusCard(voCards);

            // 多分类注入
            List<Integer> catIds = goodsToCatIds.getOrDefault(g.getId(), List.of());
            vo.setCategoryIds(catIds);
            vo.setCategories(catIds.stream().map(catMap::get).filter(Objects::nonNull).collect(Collectors.toList()));

            // 兼容旧单分类字段
            if ((vo.getCategory() == null || vo.getCategory().getId() == null) && !vo.getCategories().isEmpty()) {
                vo.setCategory(vo.getCategories().get(0));
            }

            result.add(vo);
        }

        // --- 8. 返回结构（包含分页信息） ---
        Map<String, Object> data = new HashMap<>();
        data.put("list", result);
        data.put("page", page);
        data.put("pageSize", limit);
        data.put("totalCount", total);
        data.put("cates", cates);

        return data;
    }


    // 辅助：安全把 BigDecimal 转为字符串（避免 NPE）
    private static String safeDecimalToString(BigDecimal val) {
        if (val == null) {
            return "0.00";
        }
        try {
            return val.toPlainString();
        } catch (Exception e) {
            return val.toString();
        }
    }

    // 泛用分组函数
    private static <T, K> Map<K, List<T>> groupBy(List<T> items, java.util.function.Function<T, K> keyExtractor) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyMap();
        }
        return items.stream()
                .filter(Objects::nonNull)
                .filter(i -> keyExtractor.apply(i) != null)
                .collect(Collectors.groupingBy(keyExtractor));
    }
}

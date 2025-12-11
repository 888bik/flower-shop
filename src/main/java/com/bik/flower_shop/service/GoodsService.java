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

        // 1. 多分类筛选
        List<Integer> filterGoodsIds = null;
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            filterGoodsIds = Optional.ofNullable(
                    goodsCategoryMapper.selectGoodsIdsByCategoryIds(dto.getCategoryIds())
            ).orElse(Collections.emptyList());

            if (filterGoodsIds.isEmpty()) {
                return Map.of(
                        "list", Collections.emptyList(),
                        "totalCount", 0L,
                        "cates", categoryMapper.selectList(null)
                );
            }
        }

        // 2. 构造商品查询 Wrapper
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();

        // 分类过滤
        if (filterGoodsIds != null) {
            wrapper.in("id", filterGoodsIds);
        } else if (dto.getCategoryId() != null) {
            wrapper.eq("category_id", dto.getCategoryId());
        }

        // *** 关键逻辑：删除状态筛选 ***
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

        // 搜索标题
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            wrapper.like("title", dto.getTitle().trim());
        }

        wrapper.orderByDesc("id");

        // 3. 分页查询
        Page<Goods> pageObj = new Page<>(page, limit);
        Page<Goods> goodsPage = goodsMapper.selectPage(pageObj, wrapper);
        List<Goods> goodsList = Optional.ofNullable(goodsPage.getRecords()).orElse(Collections.emptyList());

        if (goodsList.isEmpty()) {
            return Map.of(
                    "list", Collections.emptyList(),
                    "totalCount", goodsPage.getTotal(),
                    "cates", categoryMapper.selectList(null)
            );
        }

        List<Integer> goodsIds = goodsList.stream().map(Goods::getId).collect(Collectors.toList());

        // 4. 批量查询关联
        List<GoodsBanner> banners = goodsBannerMapper.selectList(new QueryWrapper<GoodsBanner>().in("goods_id", goodsIds));
        List<GoodsAttrs> attrs = goodsAttrsMapper.selectList(new QueryWrapper<GoodsAttrs>().in("goods_id", goodsIds));
        List<GoodsSkus> skus = goodsSkusMapper.selectList(new QueryWrapper<GoodsSkus>().in("goods_id", goodsIds));
        List<GoodsSkusCard> cards = goodsSkusCardMapper.selectList(new QueryWrapper<GoodsSkusCard>().in("goods_id", goodsIds));

        List<GoodsSkusCardValue> cardValues = Collections.emptyList();
        if (!cards.isEmpty()) {
            List<Integer> cardIds = cards.stream().map(GoodsSkusCard::getId).collect(Collectors.toList());
            cardValues = goodsSkusCardValueMapper.selectList(new QueryWrapper<GoodsSkusCardValue>().in("goods_skus_card_id", cardIds));
        }

        List<GoodsCategory> goodsCats = goodsCategoryMapper.selectByGoodsIds(goodsIds);

        // 5. 构造数据映射
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

        // 分类映射
        Map<Integer, List<Integer>> goodsToCatIds = goodsCats.stream()
                .collect(Collectors.groupingBy(GoodsCategory::getGoodsId,
                        Collectors.mapping(GoodsCategory::getCategoryId, Collectors.toList())));

        Set<Integer> allCatIds = goodsCats.stream().map(GoodsCategory::getCategoryId).collect(Collectors.toSet());
        Map<Integer, Category> catMap = allCatIds.isEmpty() ? Collections.emptyMap() :
                categoryMapper.selectBatchIds(new ArrayList<>(allCatIds))
                        .stream().collect(Collectors.toMap(Category::getId, c -> c));

        List<Category> cates = categoryMapper.selectList(null);

        // 6. 构建返回 VO
        List<GoodsVO> result = new ArrayList<>();
        for (Goods g : goodsList) {
            GoodsVO vo = new GoodsVO();
            BeanUtils.copyProperties(g, vo);

            vo.setMinPrice(safeDecimalToString(g.getMinPrice()));
            vo.setMinOprice(safeDecimalToString(g.getMinOprice()));
            vo.setGoodsBanner(bannerMap.getOrDefault(g.getId(), List.of()));
            vo.setGoodsAttrs(attrsMap.getOrDefault(g.getId(), List.of()));
            vo.setGoodsSkus(skusMap.getOrDefault(g.getId(), List.of()));

            // SKU Cards
            List<GoodsSkusCardVO> voCards = new ArrayList<>();
            for (GoodsSkusCard card : cardsMap.getOrDefault(g.getId(), List.of())) {
                GoodsSkusCardVO cv = new GoodsSkusCardVO();
                BeanUtils.copyProperties(card, cv);
                cv.setGoodsSkusCardValue(cardValuesMap.getOrDefault(card.getId(), List.of()));
                voCards.add(cv);
            }
            vo.setGoodsSkusCard(voCards);

            // 分类
            List<Integer> catIds = goodsToCatIds.getOrDefault(g.getId(), List.of());
            vo.setCategoryIds(catIds);
            vo.setCategories(catIds.stream().map(catMap::get).filter(Objects::nonNull).collect(Collectors.toList()));

            // 兼容单分类
            if ((vo.getCategory() == null || vo.getCategory().getId() == null) && !vo.getCategories().isEmpty()) {
                vo.setCategory(vo.getCategories().get(0));
            }

            result.add(vo);
        }

        return Map.of(
                "list", result,
                "totalCount", goodsPage.getTotal(),
                "cates", cates
        );
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

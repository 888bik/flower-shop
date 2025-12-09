package com.bik.flower_shop.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import java.lang.reflect.Method;
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


    public void checkGoods(Long id, Byte isCheck) {
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

    public void updateContent(Long id, String content) {

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
        if (id == null || dto == null) {
            return false;
        }

        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            return false;
        }

        // 更新字段
        goods.setTitle(dto.getTitle());
        goods.setCategoryId(dto.getCategoryId());
        goods.setCover(dto.getCover());
        goods.setDescription(dto.getDescription());
        goods.setUnit(dto.getUnit());
        goods.setStock(dto.getStock());
        goods.setMinStock(dto.getMinStock());
        goods.setStatus(dto.getStatus());
        goods.setStockDisplay(dto.getStockDisplay());
        goods.setMinPrice(dto.getMinPrice());
        goods.setMinOprice(dto.getMinOprice());

        goods.setUpdateTime(Instant.now().getEpochSecond());

        return goodsMapper.updateById(goods) > 0;
    }

    @Transactional
    public void deleteGoodsByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 使用 Wrapper 条件删除
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);
        goodsMapper.delete(wrapper);


//        ids.forEach(id -> {
//            Goods g = new Goods();
//            g.setId(id);
//            g.setDeleteTime(System.currentTimeMillis() / 1000L); // 秒级时间戳
//            goodsMapper.updateById(g);
//        });
    }

    /**
     * 保存商品
     */
    @Transactional
    public Goods saveGoods(Goods goods) {

        // 默认审核状态
        if (goods.getIscheck() == null) {
            goods.setIscheck((byte) 0);
        }

        // 设置创建时间和更新时间
        long now = Instant.now().getEpochSecond();
        goods.setCreateTime(now);
        goods.setUpdateTime(now);

        goodsMapper.insert(goods);
        return goods;
    }

    public Map<String, Object> listGoods(GoodsQueryDTO dto) {
        if (dto == null) {
            dto = new GoodsQueryDTO();
        }

        int page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int limit = dto.getLimit() == null || dto.getLimit() < 1 ? 10 : dto.getLimit();

        Page<Goods> p = new Page<>(page, limit);
        QueryWrapper<Goods> q = new QueryWrapper<>();

        if (dto.getTab() != null) {
            switch (dto.getTab()) {
                case "checking" -> q.eq("ischeck", 0);
                case "saling" -> q.eq("status", 1);
                case "off" -> q.eq("status", 0);
                case "min_stock" -> q.apply("stock <= min_stock");
                case "delete" -> q.isNotNull("delete_time");
            }
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            q.like("title", dto.getTitle().trim());
        }

        if (dto.getCategoryId() != null) {
            q.eq("category_id", dto.getCategoryId());
        }

        q.orderByDesc("id");

        Page<Goods> goodsPage = goodsMapper.selectPage(p, q);
        List<Goods> goodsList = Optional.ofNullable(goodsPage.getRecords()).orElse(Collections.emptyList());

        List<Category> cates = Optional.ofNullable(categoryMapper.selectList(null)).orElse(Collections.emptyList());
        if (goodsList.isEmpty()) {
            return Map.of(
                    "list", Collections.emptyList(),
                    "totalCount", goodsPage.getTotal(),
                    "cates", cates
            );
        }

        List<Integer> goodsIds = goodsList.stream()
                .map(Goods::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<GoodsBanner> banners = Optional.ofNullable(
                goodsBannerMapper.selectList(new QueryWrapper<GoodsBanner>().in("goods_id", goodsIds))
        ).orElse(Collections.emptyList());

        List<GoodsAttrs> attrs = Optional.ofNullable(
                goodsAttrsMapper.selectList(new QueryWrapper<GoodsAttrs>().in("goods_id", goodsIds))
        ).orElse(Collections.emptyList());

        // 原始 skus 行（字符串 skus 字段）
        List<GoodsSkus> skus = Optional.ofNullable(
                goodsSkusMapper.selectList(new QueryWrapper<GoodsSkus>().in("goods_id", goodsIds))
        ).orElse(Collections.emptyList());

        List<GoodsSkusCard> cards = Optional.ofNullable(
                goodsSkusCardMapper.selectList(new QueryWrapper<GoodsSkusCard>().in("goods_id", goodsIds))
        ).orElse(Collections.emptyList());

        List<GoodsSkusCardValue> cardValues;
        if (cards.isEmpty()) {
            cardValues = Collections.emptyList();
        } else {
            List<Integer> cardIds = cards.stream().map(GoodsSkusCard::getId).filter(Objects::nonNull).distinct().toList();
            cardValues = Optional.ofNullable(
                    goodsSkusCardValueMapper.selectList(new QueryWrapper<GoodsSkusCardValue>().in("goods_skus_card_id", cardIds))
            ).orElse(Collections.emptyList());
        }

        // 分组
        Map<Integer, List<GoodsBanner>> bannerMap = groupBy(banners, GoodsBanner::getGoodsId);
        Map<Integer, List<GoodsAttrs>> attrsMap = groupBy(attrs, GoodsAttrs::getGoodsId);

        // === 关键：把 List<GoodsSkus> 转为 List<GoodsSkusVO> 并按 goodsId 分组 ===
        List<GoodsSkusVO> skusVoList = skus.stream().map(s -> {
            GoodsSkusVO sv = new GoodsSkusVO();
            BeanUtils.copyProperties(s, sv);
            String raw = s.getSkus();
            if (raw != null && !raw.isBlank()) {
                try {
                    // 解析为 List<Map<String,Object>>
                    List<Map<String, Object>> parsed = JSON.parseObject(
                            raw,
                            new com.alibaba.fastjson.TypeReference<List<Map<String, Object>>>() {}
                    );
                    sv.setSkus(parsed);
                } catch (Exception ex) {
                    // 解析失败就置为 null，避免抛异常
                    sv.setSkus(null);
                }
            } else {
                sv.setSkus(null);
            }
            return sv;
        }).toList();

        Map<Integer, List<GoodsSkusVO>> skusVoMap = skusVoList.stream()
                .filter(Objects::nonNull)
                .filter(v -> v.getGoodsId() != null)
                .collect(Collectors.groupingBy(GoodsSkusVO::getGoodsId));

        Map<Integer, List<GoodsSkusCard>> cardsMap = groupBy(cards, GoodsSkusCard::getGoodsId);
        Map<Integer, List<GoodsSkusCardValue>> cardValueMap = groupBy(cardValues, GoodsSkusCardValue::getGoodsSkusCardId);
        Map<Integer, Category> categoryMap = cates.stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));

        List<GoodsVO> result = new ArrayList<>(goodsList.size());
        for (Goods g : goodsList) {
            if (g == null) {
                continue;
            }
            GoodsVO vo = new GoodsVO();
            BeanUtils.copyProperties(g, vo);

            // 处理金额
            vo.setMinPrice(safeDecimalToString(g.getMinPrice()));
            vo.setMinOprice(safeDecimalToString(g.getMinOprice()));

            // 处理时间字段
            vo.setCreateTime(g.getCreateTime());
            vo.setUpdateTime(g.getUpdateTime());
            vo.setDeleteTime(g.getDeleteTime());

            // 处理 sku（这里保留原始 sku_value 字符串，前端若需要可解析）
            vo.setSkuValue(g.getSkuValue());
            vo.setSkuType(g.getSkuType());

            // 分类
            vo.setCategory(categoryMap.getOrDefault(g.getCategoryId(), null));

            // 关联集合
            vo.setGoodsBanner(bannerMap.getOrDefault(g.getId(), List.of()));
            vo.setGoodsAttrs(attrsMap.getOrDefault(g.getId(), List.of()));

            // 使用已解析的 GoodsSkusVO Map
            vo.setGoodsSkus(skusVoMap.getOrDefault(g.getId(), List.of()));

            // skusCard + values
            List<GoodsSkusCard> cardList = cardsMap.getOrDefault(g.getId(), List.of());
            List<GoodsSkusCardVO> voCards = new ArrayList<>(cardList.size());
            for (GoodsSkusCard card : cardList) {
                if (card == null) {
                    continue;
                }
                GoodsSkusCardVO cv = new GoodsSkusCardVO();
                BeanUtils.copyProperties(card, cv);
                cv.setGoodsSkusCardValue(cardValueMap.getOrDefault(card.getId(), List.of()));
                voCards.add(cv);
            }
            vo.setGoodsSkusCard(voCards);

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

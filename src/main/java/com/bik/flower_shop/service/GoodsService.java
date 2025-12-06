package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.mapper.*;
import com.bik.flower_shop.pojo.dto.GoodsQueryDTO;
import com.bik.flower_shop.pojo.entity.*;
import com.bik.flower_shop.utils.TimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> listGoods(GoodsQueryDTO dto) {
        int page = dto.getPage() == null ? 1 : dto.getPage();
        int limit = dto.getLimit() == null ? 10 : dto.getLimit();

        Page<Goods> p = new Page<>(page, limit);

        QueryWrapper<Goods> q = new QueryWrapper<>();

        if (dto.getTab() != null) {
            switch (dto.getTab()) {
                case "checking":
                    q.eq("ischeck", 0);
                    break;
                case "saling":
                    q.eq("status", 1);
                    break;
                case "off":
                    q.eq("status", 0);
                    break;
                case "min_stock":
                    q.apply("stock <= min_stock");
                    break;
                case "delete":
                    q.isNotNull("delete_time");
                    break;
                default:
                    break;
            }
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            q.like("title", dto.getTitle());
        }
        if (dto.getCategoryId() != null) {
            q.eq("category_id", dto.getCategoryId());
        }
        q.orderByDesc("id");

        Page<Goods> goodsPage = goodsMapper.selectPage(p, q);
        List<Goods> goodsList = goodsPage.getRecords();
        if (goodsList == null || goodsList.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("list", Collections.emptyList());
            empty.put("totalCount", goodsPage.getTotal());
            empty.put("cates", categoryMapper.selectList(null));
            return empty;
        }

        // 收集 goodsId 和 categoryId
        List<Integer> goodsIds = goodsList.stream().map(Goods::getId).filter(Objects::nonNull).distinct().toList();
        Set<Integer> categoryIds = goodsList.stream()
                .map(Goods::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询相关表
        List<GoodsBanner> banners = goodsBannerMapper.selectList(new QueryWrapper<GoodsBanner>().in("goods_id", goodsIds).orderByAsc("id"));
        List<GoodsAttrs> attrs = goodsAttrsMapper.selectList(new QueryWrapper<GoodsAttrs>().in("goods_id", goodsIds).orderByAsc("`order`"));
        List<GoodsSkus> skus = goodsSkusMapper.selectList(new QueryWrapper<GoodsSkus>().in("goods_id", goodsIds).orderByAsc("id"));
        List<GoodsSkusCard> cards = goodsSkusCardMapper.selectList(new QueryWrapper<GoodsSkusCard>().in("goods_id", goodsIds).orderByAsc("`order`"));
        List<GoodsSkusCardValue> cardValues = goodsSkusCardValueMapper.selectList(new QueryWrapper<GoodsSkusCardValue>().in("goods_skus_card_id", cards.stream().map(GoodsSkusCard::getId).filter(Objects::nonNull).collect(Collectors.toList())).orderByAsc("`order`"));

        // 批量查询分类（全部返回 frontend 需要 cates）
        List<Category> cates = categoryMapper.selectList(null);
        Map<Integer, Category> categoryMap = cates.stream().collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));

        // 分组映射
        Map<Integer, List<GoodsBanner>> bannerMap = groupByGoodsId(banners, GoodsBanner::getGoodsId);
        Map<Integer, List<GoodsAttrs>> attrsMap = groupByGoodsId(attrs, GoodsAttrs::getGoodsId);
        Map<Integer, List<GoodsSkus>> skusMap = groupByGoodsId(skus, GoodsSkus::getGoodsId);
        Map<Integer, List<GoodsSkusCard>> cardsMap = groupByGoodsId(cards, GoodsSkusCard::getGoodsId);

        // cardValue 按 cardId 分组
        Map<Integer, List<GoodsSkusCardValue>> cardValueMap = new HashMap<>();
        if (cardValues != null) {
            cardValueMap = cardValues.stream().collect(Collectors.groupingBy(GoodsSkusCardValue::getGoodsSkusCardId));
        }

        // 组装商品列表
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Goods g : goodsList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", g.getId());
            item.put("title", g.getTitle());
            item.put("category_id", g.getCategoryId());
            item.put("cover", g.getCover());
            item.put("rating", g.getRating());
            item.put("sale_count", g.getSaleCount());
            item.put("review_count", g.getReviewCount());
            item.put("min_price", g.getMinPrice() != null ? g.getMinPrice().toPlainString() : "0.00");
            item.put("min_oprice", g.getMinOprice() != null ? g.getMinOprice().toPlainString() : "0.00");
            item.put("desc", g.getDescription());
            item.put("unit", g.getUnit());
            item.put("stock", g.getStock());
            item.put("min_stock", g.getMinStock());
            item.put("ischeck", g.getIscheck());
            item.put("status", g.getStatus());
            item.put("stock_display", g.getStockDisplay());
            item.put("express_id", g.getExpressId());
            item.put("sku_type", g.getSkuType());

            // sku_value
            String skuValueRaw = g.getSkuValue();
            if (skuValueRaw != null && !skuValueRaw.isBlank()) {
                try {
                    Map<?, ?> skuVal = objectMapper.readValue(skuValueRaw, Map.class);
                    item.put("sku_value", skuVal);
                } catch (Exception ex) {
                    item.put("sku_value", skuValueRaw);
                }
            } else {
                item.put("sku_value", null);
            }

            item.put("content", g.getContent());
            item.put("discount", g.getDiscount());
            item.put("create_time", TimeUtils.format(g.getCreateTime()));
            item.put("update_time", TimeUtils.format(g.getUpdateTime()));
            item.put("delete_time", g.getDeleteTime() == null ? null : TimeUtils.format(g.getDeleteTime()));
            item.put("order", g.getOrder());

            // category
            Category cat = categoryMap.get(g.getCategoryId());
            if (cat != null) {
                Map<String, Object> cm = new HashMap<>();
                cm.put("id", cat.getId());
                cm.put("name", cat.getName());
                cm.put("status", cat.getStatus());
                cm.put("create_time", TimeUtils.format(cat.getCreateTime()));
                cm.put("update_time", TimeUtils.format(cat.getUpdateTime()));
                cm.put("category_id", cat.getCategoryId());
                cm.put("order", cat.getOrder());
                item.put("category", cm);
            } else {
                item.put("category", null);
            }

            // banners/attrs/skus/cards(values)
            item.put("goods_banner", bannerMap.getOrDefault(g.getId(), Collections.emptyList()));
            item.put("goods_attrs", attrsMap.getOrDefault(g.getId(), Collections.emptyList()));
            item.put("goods_skus", skusMap.getOrDefault(g.getId(), Collections.emptyList()));

            List<GoodsSkusCard> cardList = cardsMap.getOrDefault(g.getId(), Collections.emptyList());
            List<Map<String, Object>> cardMaps = new ArrayList<>();
            for (GoodsSkusCard c : cardList) {
                Map<String, Object> cm = new HashMap<>();
                cm.put("id", c.getId());
                cm.put("goods_id", c.getGoodsId());
                cm.put("name", c.getName());
                cm.put("type", c.getType());
                cm.put("order", c.getOrder());
                List<GoodsSkusCardValue> vals = cardValueMap.getOrDefault(c.getId(), Collections.emptyList());
                cm.put("goods_skus_card_value", vals);
                cardMaps.add(cm);
            }
            item.put("goods_skus_card", cardMaps);

            resultList.add(item);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("list", resultList);
        res.put("totalCount", goodsPage.getTotal());
        res.put("cates", cates);

        return res;
    }

    // helper grouping by goodsId
    private static <T> Map<Integer, List<T>> groupByGoodsId(List<T> list, java.util.function.Function<T, Integer> extractor) {
        if (list == null) {
            return Collections.emptyMap();
        }
        return list.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(extractor));
    }
}

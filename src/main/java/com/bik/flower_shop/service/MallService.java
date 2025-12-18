package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bik.flower_shop.exception.BusinessException;
import com.bik.flower_shop.mapper.CategoryMapper;
import com.bik.flower_shop.mapper.GoodsBannerMapper;
import com.bik.flower_shop.mapper.GoodsCategoryMapper;
import com.bik.flower_shop.mapper.GoodsMapper;
import com.bik.flower_shop.pojo.DeliveryDTO;
import com.bik.flower_shop.pojo.dto.*;
import com.bik.flower_shop.pojo.entity.Category;
import com.bik.flower_shop.pojo.entity.Goods;
import com.bik.flower_shop.pojo.entity.GoodsBanner;
import com.bik.flower_shop.pojo.entity.GoodsCategory;
import com.bik.flower_shop.pojo.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@RequiredArgsConstructor
@Service
public class MallService {

    private final GoodsMapper goodsMapper;
    private final GoodsBannerMapper goodsBannerMapper;
    private final GoodsCategoryMapper goodsCategoryMapper;
    private final CategoryMapper categoryMapper;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Map<String, Object> listMallGoods(MallQueryDTO dto) {
        int page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int limit = dto.getLimit() == null || dto.getLimit() < 1 ? 12 : dto.getLimit();

        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).isNull("delete_time");

        if (dto.getCategoryId() != null) {
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

        wrapper.select("id", "title", "cover", "min_price", "min_oprice", "rating", "sale_count", "stock", "unit", "sku_type");
        wrapper.orderByDesc("id");

        Page<Map<String, Object>> pageObj = new Page<>(page, limit);
        IPage<Map<String, Object>> pageResult = goodsMapper.selectMapsPage(pageObj, wrapper);
        List<Map<String, Object>> records = Optional.ofNullable(pageResult.getRecords()).orElse(Collections.emptyList());

        List<Integer> ids = records.stream()
                .map(m -> (Integer) m.get("id"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 1. 批量查询 banners
        List<GoodsBanner> banners = ids.isEmpty() ? Collections.emptyList()
                : goodsBannerMapper.selectList(new QueryWrapper<GoodsBanner>().in("goods_id", ids).select("goods_id", "url"));
        Map<Integer, List<String>> bannerMap = banners.stream()
                .collect(Collectors.groupingBy(GoodsBanner::getGoodsId, Collectors.mapping(GoodsBanner::getUrl, Collectors.toList())));

        // 2. 批量查询商品分类
        List<GoodsCategory> goodsCats = ids.isEmpty() ? Collections.emptyList()
                : goodsCategoryMapper.selectByGoodsIds(ids);
        Set<Integer> catIds = goodsCats.stream().map(GoodsCategory::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, Category> catMap = catIds.isEmpty() ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(new ArrayList<>(catIds)).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // 3. 构建商品 -> 分类映射
        Map<Integer, List<Map<String, Object>>> goodsToCats = goodsCats.stream()
                .collect(Collectors.groupingBy(GoodsCategory::getGoodsId,
                        Collectors.mapping(gc -> {
                            Category c = catMap.get(gc.getCategoryId());
                            if (c == null) {
                                return null;
                            }
                            Map<String, Object> m = new HashMap<>();
                            m.put("id", c.getId());
                            m.put("name", c.getName());
                            return m;
                        }, Collectors.toList())
                ));

        // 4. 组装返回 list
        List<Map<String, Object>> mallList = new ArrayList<>();
        for (Map<String, Object> row : records) {
            Integer gid = (Integer) row.get("id");
            Map<String, Object> item = new HashMap<>();
            item.put("id", gid);
            item.put("title", row.get("title"));
            item.put("cover", row.get("cover"));
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


    public List<Category> listCategories(String type, Integer parentId, Byte status) {
        QueryWrapper<Category> qw = new QueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            qw.eq("type", type);
        }
        if (parentId != null) {
            qw.eq("category_id", parentId);
        }
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByAsc("`order`").orderByDesc("create_time");
        return categoryMapper.selectList(qw);
    }

    public GoodsDetailDTO toFrontendDto(GoodsVO vo, Integer userId) {
        if (vo == null) {
            return null;
        }
        GoodsDetailDTO dto = new GoodsDetailDTO();
        dto.setId(vo.getId());
        dto.setTitle(vo.getTitle());
        dto.setSubtitle(null);
        dto.setLikeCount(vo.getLikeCount() == null ? 0 : vo.getLikeCount());

        // 判断当前用户是否收藏
        if (userId != null) {
            boolean favorite = userService.isFavorite(userId, vo.getId());
            dto.setIsFavorite(favorite);
        } else {
            dto.setIsFavorite(false);
        }


        if (vo.getCategory() != null) {
            SimpleCategoryDTO c = new SimpleCategoryDTO();
            c.setId(vo.getCategory().getId());
            c.setName(vo.getCategory().getName());
            dto.setCategory(c);
        }
        dto.setCover(vo.getCover());


        if (vo.getGoodsBanner() != null) {
            List<String> banners = vo.getGoodsBanner().stream()
                    .map(GoodsBanner::getUrl)
                    .collect(Collectors.toList());
            dto.setBanners(banners);
        } else {
            dto.setBanners(Collections.emptyList());
        }

        PriceDTO price = new PriceDTO();
        price.setMin(safeParseBigDecimal(vo.getMinPrice()));
        price.setOriginalMin(safeParseBigDecimal(vo.getMinOprice()));
        price.setDiscount(vo.getDiscount() == null ? BigDecimal.ZERO : vo.getDiscount());
        price.setDisplayMin(formatPrice(price.getMin()));
        dto.setPrice(price);


        SkuDTO skuDto = new SkuDTO();
        skuDto.setType(vo.getSkuType());
        List<SkuItemDTO> items = new ArrayList<>();
        if (vo.getGoodsSkus() != null) {
            for (GoodsSkusVO s : vo.getGoodsSkus()) {
                SkuItemDTO it = new SkuItemDTO();
                it.setSkuId(s.getId());
                it.setImage(s.getImage());
                it.setPrice(s.getPprice() != null ? s.getPprice() : BigDecimal.ZERO);
                it.setStock(s.getStock() != null ? s.getStock() : 0);
                if (s.getSkus() != null) {
                    List<SkuSpecDTO> specs = ((List<Map<String, Object>>) s.getSkus()).stream()
                            .map(m -> {
                                SkuSpecDTO sp = new SkuSpecDTO();
                                sp.setKey((String) m.getOrDefault("name", m.get("key")));
                                sp.setValue(String.valueOf(m.get("value") != null ? m.get("value") : m.get("text")));
                                return sp;
                            }).collect(Collectors.toList());
                    it.setSpecs(specs);
                } else {
                    it.setSpecs(Collections.emptyList());
                }
                items.add(it);
            }
        }
        skuDto.setItems(items);

        if (vo.getGoodsSkusCard() != null) {
            List<SkuCardDTO> cards = vo.getGoodsSkusCard().stream().map(card -> {
                SkuCardDTO cd = new SkuCardDTO();
                cd.setCardId(card.getId());
                cd.setName(card.getName());
                List<SkuCardValueDTO> vals = Optional.ofNullable(card.getGoodsSkusCardValue()).orElse(Collections.emptyList())
                        .stream().map(v -> {
                            SkuCardValueDTO tv = new SkuCardValueDTO();
                            tv.setId(v.getId());
                            tv.setValue(v.getValue());
                            return tv;
                        }).collect(Collectors.toList());
                cd.setValues(vals);
                return cd;
            }).collect(Collectors.toList());
            skuDto.setCards(cards);
        } else {
            skuDto.setCards(Collections.emptyList());
        }

        dto.setSku(skuDto);

        // stock
        StockDTO stock = new StockDTO();
        stock.setTotal(vo.getStock() == null ? 0 : vo.getStock());
        stock.setDisplay(vo.getStockDisplay() != null && vo.getStockDisplay() == 1);
        stock.setMinStock(vo.getMinStock());
        dto.setStock(stock);

        // sales
        SalesDTO sales = new SalesDTO();
        sales.setSaleCount(vo.getSaleCount() == null ? 0 : vo.getSaleCount());
        sales.setReviewCount(vo.getReviewCount() == null ? 0 : vo.getReviewCount());
        sales.setRating(vo.getRating() == null ? BigDecimal.ZERO : vo.getRating());
        dto.setSales(sales);

        dto.setUnit(vo.getUnit());
        dto.setContentHtml(vo.getContent());

        DeliveryDTO delivery = new DeliveryDTO();
        delivery.setExpressId(vo.getExpressId());
        delivery.setFee(BigDecimal.ZERO);
        dto.setDelivery(delivery);

        // isAvailable: 上架且通过审核 且 库存>0 (你可以按业务调整)
        boolean available = (vo.getStatus() != null && vo.getStatus() == 1)
                && (vo.getIscheck() != null && vo.getIscheck() == 1)
                && (vo.getStock() == null || vo.getStock() > 0);
        dto.setIsAvailable(available);

        dto.setCreateTime(vo.getCreateTime() == null ? null : vo.getCreateTime());
        return dto;
    }

    // helper
    private BigDecimal safeParseBigDecimal(String s) {
        try {
            return s == null ? null : new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatPrice(BigDecimal p) {
        if (p == null) {
            return null;
        }
        return "¥" + p.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }


}

package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bik.flower_shop.mapper.GoodsBannerMapper;
import com.bik.flower_shop.mapper.GoodsMapper;
import com.bik.flower_shop.mapper.HomeFloorMapper;
import com.bik.flower_shop.pojo.entity.Goods;
import com.bik.flower_shop.pojo.entity.HomeFloor;
import com.bik.flower_shop.pojo.vo.GoodsSimpleVO;
import com.bik.flower_shop.pojo.vo.HomeFloorVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@Service
@RequiredArgsConstructor
public class HomeService {

    private final HomeFloorMapper homeFloorMapper;
    private final GoodsMapper goodsMapper;
    private final GoodsBannerMapper goodsBannerMapper;

    public List<HomeFloorVO> getHomeFloors() {

        List<HomeFloor> floors = homeFloorMapper.selectList(
                new LambdaQueryWrapper<HomeFloor>()
                        .eq(HomeFloor::getStatus, 1)
                        .orderByAsc(HomeFloor::getSort)
        );

        if (floors.isEmpty()) {
            return List.of();
        }

        List<HomeFloorVO> result = new ArrayList<>();


        for (HomeFloor floor : floors) {

            // 查楼层下的商品
            List<Goods> goodsList = goodsMapper.selectHomeGoods(
                    floor.getCategoryId(),
                    floor.getProductLimit()
            );

            if (goodsList.isEmpty()) {
                continue;
            }

            // 收集商品ID
            List<Integer> goodsIds = goodsList.stream()
                    .map(Goods::getId)
                    .toList();

            // 查询轮播图
            String ids = goodsIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            List<Map<String, Object>> banners =
                    goodsBannerMapper.selectByGoodsIds(ids);

            // 按 goods_id 分组
            Map<Integer, List<String>> bannerMap = banners.stream()
                    .collect(Collectors.groupingBy(
                            m -> (Integer) m.get("goods_id"),
                            Collectors.mapping(
                                    m -> (String) m.get("url"),
                                    Collectors.toList()
                            )
                    ));

            // 组装商品 VO
            List<GoodsSimpleVO> products = goodsList.stream().map(g -> {
                GoodsSimpleVO vo = new GoodsSimpleVO();
                vo.setId(g.getId());
                vo.setTitle(g.getTitle());
                vo.setCover(g.getCover());
                vo.setMinPrice(g.getMinPrice());
                vo.setMinOprice(g.getMinOprice());
                vo.setBanners(bannerMap.getOrDefault(g.getId(), List.of()));
                vo.setSaleCount(g.getSaleCount());
                vo.setRating(g.getRating());
                return vo;
            }).toList();

            // 组装楼层 VO
            HomeFloorVO floorVO = new HomeFloorVO();
            floorVO.setId(floor.getId());
            floorVO.setTitle(floor.getTitle());
            floorVO.setCategoryId(floor.getCategoryId());
            floorVO.setBannerImage(floor.getBannerImage());
            floorVO.setProducts(products);

            result.add(floorVO);
        }

        return result;
    }
}

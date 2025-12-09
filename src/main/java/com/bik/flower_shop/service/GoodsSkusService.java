package com.bik.flower_shop.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.mapper.GoodsMapper;
import com.bik.flower_shop.mapper.GoodsSkusCardMapper;
import com.bik.flower_shop.mapper.GoodsSkusCardValueMapper;
import com.bik.flower_shop.mapper.GoodsSkusMapper;
import com.bik.flower_shop.pojo.dto.*;
import com.bik.flower_shop.pojo.entity.Goods;
import com.bik.flower_shop.pojo.entity.GoodsSkus;
import com.bik.flower_shop.pojo.entity.GoodsSkusCard;
import com.bik.flower_shop.pojo.entity.GoodsSkusCardValue;
import com.bik.flower_shop.pojo.vo.GoodsSkusCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author: bik
 * Date: 2025-12-07
 * Time: 21:14
 */
@RequiredArgsConstructor
@Service
public class GoodsSkusService {

    private final GoodsMapper goodsMapper;
    private final GoodsSkusMapper goodsSkusMapper;
    private final GoodsSkusCardMapper goodsSkusCardMapper;
    private final GoodsSkusCardValueMapper goodsSkusCardValueMapper;


    @Transactional
    public Map<String, Object> setGoodsSkusCardValues(Integer cardId, SetGoodsSkusCardDTO dto) {
        if (cardId == null) {
            throw new IllegalArgumentException("规格卡片 id 不能为空");
        }
        if (dto == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("name 不能为空");
        }

        // 1. 查卡片是否存在
        GoodsSkusCard card = goodsSkusCardMapper.selectById(cardId);
        if (card == null) {

            throw new RuntimeException("规格卡片不存在: id=" + cardId);
        }

        // 2. 更新卡片名称（如果有变化）
        if (!dto.getName().equals(card.getName())) {
            card.setName(dto.getName().trim());
            goodsSkusCardMapper.updateById(card);
        }

        // 3. 删除该卡片下所有旧的规格值，避免残留
        goodsSkusCardValueMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GoodsSkusCardValue>()
                        .eq("goods_skus_card_id", cardId)
        );

        // 4. 插入新的值（如果 value 为 null 或空 -> 表示清空所有值 -> 返回空列表）
        List<GoodsSkusCardValue> inserted = new ArrayList<>();
        if (dto.getValue() != null && !dto.getValue().isEmpty()) {
            for (String v : dto.getValue()) {
                if (v == null) {
                    continue;
                }
                GoodsSkusCardValue val = new GoodsSkusCardValue();
                val.setGoodsSkusCardId(cardId);
                val.setName(dto.getName().trim());
                val.setValue(v.trim());
                val.setOrder(50);
                goodsSkusCardValueMapper.insert(val);
                inserted.add(val);
            }
        }

        // 5. 返回结果：card + 新插入的 values
        Map<String, Object> resp = new HashMap<>();
        resp.put("goodsSkusCard", card);
        resp.put("goodsSkusCardValue", inserted);
        return resp;
    }


    @Transactional
    public boolean updateGoodsSkusCardValue(Integer id, UpdateGoodsSkusCardValueDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("id 不能为空");
        }
        if (dto == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }

        // 1. 查询是否存在
        GoodsSkusCardValue exist = goodsSkusCardValueMapper.selectById(id);
        if (exist == null) {
            // 不存在返回 false
            return false;
        }

        // 2. 校验规格卡片是否存在
        if (dto.getGoodsSkusCardId() != null) {
            GoodsSkusCard card = goodsSkusCardMapper.selectById(dto.getGoodsSkusCardId());
            if (card == null) {
                throw new RuntimeException("规格卡片不存在: id=" + dto.getGoodsSkusCardId());
            }
            exist.setGoodsSkusCardId(dto.getGoodsSkusCardId());
        }

        // 3. 更新可变字段
        if (dto.getName() != null) {
            exist.setName(dto.getName().trim());
        }
        if (dto.getValue() != null) {
            exist.setValue(dto.getValue().trim());
        }
        if (dto.getOrder() != null) {
            exist.setOrder(dto.getOrder());
        }

        // 4. 执行更新
        int rows = goodsSkusCardValueMapper.updateById(exist);
        if (rows == 0) {
            throw new RuntimeException("更新规格值失败");
        }

        return true;
    }


    @Transactional
    public boolean deleteGoodsSkusCardValue(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id 不能为空");
        }

        // 1. 检查规格值是否存在
        GoodsSkusCardValue value = goodsSkusCardValueMapper.selectById(id);
        if (value == null) {
            // 不存在直接返回 false
            return false;
        }

        // 2. 删除该规格值
        int rows = goodsSkusCardValueMapper.deleteById(id);

        if (rows == 0) {
            throw new RuntimeException("删除规格值失败");
        }

        return true;
    }


    @Transactional
    public GoodsSkusCardValue createGoodsSkusCardValue(CreateGoodsSkusCardValueDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (dto.getGoodsSkusCardId() == null) {
            throw new IllegalArgumentException("goodsSkusCardId 不能为空");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("name 不能为空");
        }

        // 校验父卡片存在
        GoodsSkusCard card = goodsSkusCardMapper.selectById(dto.getGoodsSkusCardId());
        if (card == null) {
            throw new RuntimeException("规格卡片不存在");
        }

        GoodsSkusCardValue val = new GoodsSkusCardValue();
        val.setGoodsSkusCardId(dto.getGoodsSkusCardId());
        val.setName(dto.getName().trim());
        val.setValue(dto.getValue() == null ? null : dto.getValue().trim());
        val.setOrder(dto.getOrder() == null ? 50 : dto.getOrder());

        goodsSkusCardValueMapper.insert(val);

        // insert 后，MyBatis 会把自增 id 回填到 val（如果 mapper/DB 配置正常）
        return val;
    }


    @Transactional
    public GoodsSkusCardVO updateGoodsSkusCard(Integer id, UpdateGoodsSkusCardDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("id 不能为空");
        }
        if (dto == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }

        GoodsSkusCard card = goodsSkusCardMapper.selectById(id);
        if (card == null) {
            throw new RuntimeException("规格卡片不存在");
        }

        // 更新可变字段（只在 DTO 提供时更新）
        if (dto.getGoodsId() != null) {
            card.setGoodsId(dto.getGoodsId());
        }
        if (dto.getName() != null) {
            card.setName(dto.getName().trim());
        }
        if (dto.getOrder() != null) {
            card.setOrder(dto.getOrder());
        }
        if (dto.getType() != null) {
            // 兼容：前端传 number（0/1），实体中是 Boolean（或 Byte）——以现有实体为准：
            // 如果实体的 type 是 Boolean，则把 dto.type != 0 -> true/false
            try {
                card.setType(dto.getType());
            } catch (Exception e) {
                // 如果你的实体 type 不是 Boolean（例如 Byte），请改用下面注释代码：

                throw new RuntimeException("类型转换失败，请检查 GoodsSkusCard.type 类型", e);
            }
        }

        int rows = goodsSkusCardMapper.updateById(card);
        if (rows == 0) {
            throw new RuntimeException("更新失败");
        }

        // 读取并组装返回 VO（带 values）
        GoodsSkusCardVO vo = new GoodsSkusCardVO();
        BeanUtils.copyProperties(card, vo);

        // 取出该卡片下的 values
        List<GoodsSkusCardValue> values = Optional.ofNullable(
                goodsSkusCardValueMapper.selectList(
                        new QueryWrapper<GoodsSkusCardValue>().eq("goods_skus_card_id", id)
                )
        ).orElse(Collections.emptyList());

        vo.setGoodsSkusCardValue(values);

        return vo;
    }

    @Transactional
    public boolean deleteGoodsSkusCard(Integer cardId) {
        if (cardId == null) {
            return false;
        }

        // 1. 查询规格卡片是否存在
        GoodsSkusCard card = goodsSkusCardMapper.selectById(cardId);
        if (card == null) {
            return false;
        }

        // 2. 删除该规格卡片下的所有规格值 goods_skus_card_value
        goodsSkusCardValueMapper.delete(
                new QueryWrapper<GoodsSkusCardValue>()
                        .eq("goods_skus_card_id", cardId)
        );

        // 3. 删除规格卡片 goods_skus_card
        goodsSkusCardMapper.deleteById(cardId);

        return true;
    }


    @Transactional
    public void updateGoodsSkus(Long goodsId, UpdateSkusDTO dto) {
        if (goodsId == null) {
            throw new IllegalArgumentException("goodsId 不能为空");
        }
        if (dto == null || dto.getSkuType() == null) {
            throw new IllegalArgumentException("skuType 不能为空");
        }

        Goods goods = goodsMapper.selectById(goodsId.intValue());
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        // --- 单规格 ---
        if (dto.getSkuType() == 0) {
            goods.setSkuType((byte) 0);

            // 仅当前端明确给了 skuValue 时才序列化保存到 goods.sku_value（varchar）
            if (dto.getSkuValue() != null) {
                // JSON.toJSONString 会把数值以数字形式序列化：{"cprice":200,"oprice":200,...}
                goods.setSkuValue(JSON.toJSONString(dto.getSkuValue()));
            }
            goodsMapper.updateById(goods);

        /* 重要：针对 goods_skus 的处理策略
           - 如果 dto.getGoodsSkus() 为 null 或为空列表 []：都 **不** 对 goods_skus 表做任何改动（保留已有行）
           - 只有当 dto.getGoodsSkus() 非空（前端明确传入了要替换的 sku 行）时，才删除并插入新的 goods_skus
        */
            if (dto.getGoodsSkus() != null && !dto.getGoodsSkus().isEmpty()) {
                // 前端显式传来了sku行 -> 用传入的数据替换 goods_skus
                goodsSkusMapper.deleteByGoodsId(goodsId.intValue());
                for (UpdateSkusDTO.GoodsSkuDTO skuDto : dto.getGoodsSkus()) {
                    GoodsSkus sku = new GoodsSkus();
                    sku.setGoodsId(goodsId.intValue());
                    sku.setOprice(skuDto.getOprice());
                    sku.setPprice(skuDto.getPprice());
                    sku.setCprice(skuDto.getCprice());
                    sku.setWeight(skuDto.getWeight());
                    sku.setVolume(skuDto.getVolume());
                    sku.setStock(skuDto.getStock() == null ? 0 : skuDto.getStock());
                    sku.setCode(skuDto.getCode());
                    sku.setImage(skuDto.getImage());
                    // 将 skus (Map/List/Object) 序列化为 JSON 字符串存储（如果为 null 则存 null）
                    sku.setSkus(skuDto.getSkus() != null ? JSON.toJSONString(skuDto.getSkus()) : null);
                    goodsSkusMapper.insert(sku);
                }
            }

            return;
        }

        // --- 多规格 ---
        if (dto.getSkuType() == 1) {
            goods.setSkuType((byte) 1);
            // 多规格不使用 sku_value 字段
            goods.setSkuValue(null);
            goodsMapper.updateById(goods);

            // 只有前端传入了 goodsSkusCard 才替换卡片（否则保留原有卡片）
            if (dto.getGoodsSkusCard() != null) {
                // 删除旧卡片及其值
                List<GoodsSkusCard> existedCards = goodsSkusCardMapper.selectList(
                        new QueryWrapper<GoodsSkusCard>().eq("goods_id", goodsId)
                );
                if (!existedCards.isEmpty()) {
                    List<Integer> existedCardIds = existedCards.stream().map(GoodsSkusCard::getId).collect(Collectors.toList());
                    goodsSkusCardValueMapper.delete(new QueryWrapper<GoodsSkusCardValue>().in("goods_skus_card_id", existedCardIds));
                    goodsSkusCardMapper.delete(new QueryWrapper<GoodsSkusCard>().eq("goods_id", goodsId));
                }

                // 插入新的卡片及卡片值
                for (UpdateSkusDTO.GoodsSkusCardDTO cardDto : dto.getGoodsSkusCard()) {
                    GoodsSkusCard card = new GoodsSkusCard();
                    card.setGoodsId(goodsId.intValue());
                    card.setName(cardDto.getName());
                    card.setType(cardDto.getType());
                    card.setOrder(cardDto.getOrder() == null ? 50 : cardDto.getOrder());
                    goodsSkusCardMapper.insert(card);

                    if (cardDto.getGoodsSkusCardValue() != null && !cardDto.getGoodsSkusCardValue().isEmpty()) {
                        for (UpdateSkusDTO.GoodsSkusCardValueDTO valDto : cardDto.getGoodsSkusCardValue()) {
                            GoodsSkusCardValue val = new GoodsSkusCardValue();
                            val.setGoodsSkusCardId(card.getId());
                            val.setName(valDto.getName());
                            val.setValue(valDto.getValue());
                            val.setOrder(valDto.getOrder() == null ? 50 : valDto.getOrder());
                            goodsSkusCardValueMapper.insert(val);
                        }
                    }
                }
            }

            // goods_skus（sku 行）：只有当 DTO 提供了 goodsSkus 时才替换（否则保持原有 sku 行）
            if (dto.getGoodsSkus() != null) {
                // 如果是空数组 -> 视为“前端明确表示不想保留任何 sku 行”？ 这里我们采取：只有非空才替换，
                // 若希望空数组表示“删除所有 sku 行”，可以把判断改为 dto.getGoodsSkus() != null （允许空数组）
                if (!dto.getGoodsSkus().isEmpty()) {
                    goodsSkusMapper.deleteByGoodsId(goodsId.intValue());
                    for (UpdateSkusDTO.GoodsSkuDTO skuDto : dto.getGoodsSkus()) {
                        GoodsSkus sku = new GoodsSkus();
                        sku.setGoodsId(goodsId.intValue());
                        sku.setOprice(skuDto.getOprice());
                        sku.setPprice(skuDto.getPprice());
                        sku.setCprice(skuDto.getCprice());
                        sku.setWeight(skuDto.getWeight());
                        sku.setVolume(skuDto.getVolume());
                        sku.setStock(skuDto.getStock() == null ? 0 : skuDto.getStock());
                        sku.setCode(skuDto.getCode());
                        sku.setImage(skuDto.getImage());
                        sku.setSkus(skuDto.getSkus() != null ? JSON.toJSONString(skuDto.getSkus()) : null);
                        goodsSkusMapper.insert(sku);
                    }
                }
            }

            return;
        }

        throw new IllegalArgumentException("不支持的 skuType：" + dto.getSkuType());
    }


    @Transactional
    public GoodsSkusCard createGoodsSkusCard(AddGoodsSkusCardDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("参数为空");
        }
        if (dto.getGoodsId() == null) {
            throw new IllegalArgumentException("goodsId 不能为空");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("name 不能为空");
        }

        // 可选：检查商品是否存在
        Goods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品不存在: id=" + dto.getGoodsId());
        }

        GoodsSkusCard card = new GoodsSkusCard();
        card.setGoodsId(dto.getGoodsId());
        card.setName(dto.getName());
        card.setOrder(dto.getOrder() == null ? 50 : dto.getOrder());

        card.setType(dto.getType());

        // 插入
        goodsSkusCardMapper.insert(card);

        // 返回插入后的实体（包含 id）
        return card;
    }

    @Transactional
    public boolean sortGoodsSkus(UpdateGoodsSkusOrderDTO dto) {
        try {
            int updated = goodsSkusCardMapper.updateBatchOrder(dto.getSortData());
            return updated == dto.getSortData().size();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 避免对已经是 JSON 字符串的数据再次序列化为 JSON（防止二次编码 / 引号转义问题）
     * 支持 skus 为：null / String（原始 JSON 或普通字符串） / Map / List / POJO
     */
    private String normalizeSkusForDb(Object skus) {
        if (skus == null) {
            return null;
        }
        if (skus instanceof String) {
            String s = ((String) skus).trim();
            if (s.isEmpty()) {
                return null;
            }
            // 已经是 JSON（数组或对象）直接返回原串，避免二次编码
            if ((s.startsWith("[") && s.endsWith("]")) || (s.startsWith("{") && s.endsWith("}"))) {
                return s;
            }
            // 普通字符串：序列化为 JSON 字符串（保证 DB 中为合法 JSON）
            return JSON.toJSONString(s);
        }
        // 其它类型（Map/List/POJO）直接序列化成 JSON
        return JSON.toJSONString(skus);
    }

    // 将 skus 字符串解析为对象（List/Map/原始字符串）
    private Object parseSkusFromDb(String skus) {
        if (skus == null) {
            return null;
        }
        String s = skus.trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            if (s.startsWith("[")) {
                // JSON 数组
                return JSON.parseArray(s);
            } else if (s.startsWith("{")) {
                // JSON 对象
                return JSON.parseObject(s);
            } else {
                // 普通字符串（非 JSON）
                return s;
            }
        } catch (Exception e) {
            // 万一解析失败，返回原始字符串
            return s;
        }
    }

}
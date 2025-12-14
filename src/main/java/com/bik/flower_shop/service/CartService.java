package com.bik.flower_shop.service;// package com.bik.flower_shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bik.flower_shop.mapper.CartMapper;
import com.bik.flower_shop.mapper.GoodsMapper;
import com.bik.flower_shop.pojo.dto.AddCartDTO;
import com.bik.flower_shop.pojo.dto.UpdateCartNumDTO;
import com.bik.flower_shop.pojo.entity.Cart;
import com.bik.flower_shop.pojo.entity.Goods;
import com.bik.flower_shop.pojo.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author bik
 */
@RequiredArgsConstructor
@Service
public class CartService {
    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;

    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Integer userId, AddCartDTO dto) {
        if (userId == null) {
            throw new IllegalArgumentException("userId required");
        }
        if (dto.getGoodsId() == null) {
            throw new IllegalArgumentException("goodsId required");
        }
        int num = dto.getNum() == null || dto.getNum() <= 0 ? 1 : dto.getNum();

        // 1. 检查商品是否存在
        Goods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        // 2. 简单库存校验（若有 sku 表请用 sku 库存）
        // 全局库存（若是多规格，应使用 sku 表）
        Integer availableStock = goods.getStock();
        if (availableStock != null && availableStock < num) {
            throw new IllegalArgumentException("库存不足");
        }

        // 3. 查找是否已存在相同购物车项（相同 userId + goodsId + skuId）
        QueryWrapper<Cart> q = new QueryWrapper<>();
        q.eq("user_id", userId).eq("goods_id", dto.getGoodsId());
        if (dto.getSkuId() != null) {
            q.eq("sku_id", dto.getSkuId());
        } else {
            q.eq("sku_id", 0);
        }

        Cart exist = cartMapper.selectOne(q);

        long now = Instant.now().getEpochSecond();

        if (exist != null) {
            // 合并数量 — 注意并发场景请用行锁或乐观锁
            exist.setNum(exist.getNum() + num);
            exist.setUpdateTime(now);
            cartMapper.updateById(exist);
        } else {
            Cart c = new Cart();
            c.setUserId(userId);
            c.setGoodsId(dto.getGoodsId());
            c.setSkuId(dto.getSkuId() == null ? 0 : dto.getSkuId());
            c.setNum(num);
            c.setSkusType(Objects.equals(goods.getSkuType(), (byte) 1) || Boolean.parseBoolean(String.valueOf(goods.getSkuType())));
            c.setCreateTime(now);
            c.setUpdateTime(now);
            cartMapper.insert(c);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCartNum(Integer userId, UpdateCartNumDTO dto) {
        if (userId == null || dto.getCartId() == null || dto.getNum() == null || dto.getNum() < 1) {
            throw new IllegalArgumentException("参数错误");
        }

        // 查购物车项
        Cart cart = cartMapper.selectById(dto.getCartId());
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new IllegalArgumentException("购物车项不存在");
        }

        // 校验库存
        Goods goods = goodsMapper.selectById(cart.getGoodsId());
        if (goods != null && goods.getStock() < dto.getNum()) {
            throw new IllegalArgumentException("库存不足");
        }

        cart.setNum(dto.getNum());
        cart.setUpdateTime(Instant.now().getEpochSecond());
        cartMapper.updateById(cart);
    }


    /**
     * 返回富结构的购物车列表（Cart + Goods 聚合）
     */
    public List<CartItemVO> listCartVO(Integer userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        QueryWrapper<Cart> q = new QueryWrapper<>();
        q.eq("user_id", userId).orderByDesc("update_time");
        List<Cart> carts = cartMapper.selectList(q);
        if (carts == null || carts.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量取 goods
        Set<Integer> goodsIds = carts.stream()
                .map(Cart::getGoodsId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Goods> goodsList = goodsMapper.selectBatchIds(goodsIds);
        Map<Integer, Goods> goodsMap = goodsList.stream()
                .collect(Collectors.toMap(Goods::getId, g -> g));

        List<CartItemVO> vos = new ArrayList<>(carts.size());
        for (Cart c : carts) {
            CartItemVO vo = new CartItemVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setGoodsId(c.getGoodsId());
            vo.setSkuId(c.getSkuId());
            vo.setSkusType(c.getSkusType());
            vo.setNum(c.getNum());
            vo.setCreateTime(c.getCreateTime());
            vo.setUpdateTime(c.getUpdateTime());

            Goods g = goodsMap.get(c.getGoodsId());
            if (g != null) {
                vo.setTitle(g.getTitle());
                vo.setCover(g.getCover());
                vo.setUnit(g.getUnit());
                vo.setDescription(g.getDescription());
                vo.setStock(g.getStock());
                vo.setSaleCount(g.getSaleCount());
                vo.setRating(g.getRating() == null ? null : g.getRating().doubleValue());
                // 把 BigDecimal 转字符串，前端更方便显示
                vo.setMinPrice(g.getMinPrice() == null ? "0.00" : g.getMinPrice().toString());
                vo.setMinOprice(g.getMinOprice() == null ? null : g.getMinOprice().toString());
            } else {
                // 商品已被删除或下架
                vo.setValid(false);
            }

            // 如果你有 sku 表，可以在这里查询 sku 并覆盖价格/stock
            // if (c.getSkuId() != null && c.getSkuId() > 0) { ... }

            vos.add(vo);
        }

        return vos;
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeFromCart(Integer userId, Integer cartId) {
        if (cartId == null) {
            return;
        }
        Cart c = cartMapper.selectById(cartId);
        if (c == null) {
            throw new IllegalArgumentException("购物车项不存在");
        }
        if (!Objects.equals(c.getUserId(), userId)) {
            throw new IllegalArgumentException("无权限删除");
        }
        cartMapper.deleteById(cartId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Integer userId) {
        QueryWrapper<Cart> q = new QueryWrapper<>();
        q.eq("user_id", userId);
        cartMapper.delete(q);
    }
}

package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态查询购物车数据
     * @param shoppingCart
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);
    /**
     * 根据ID修改购物车数据
     * @param shoppingCart
     */
    void upodate(ShoppingCart shoppingCart);
    /**
     * 添加购物车数据
     * @param cart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "VALUES (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime})")
    void insert(ShoppingCart cart);
    /**
     * 根据用户id删除购物车数据
     * @param currentId
     */
    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void deleteByUserId(Long currentId);
    /**
     * 根据id删除对应的购物车数据
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);
}

package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    @Transactional // 开启事务，确保对多个表数据的修改操作是原子性
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        //数据拷贝
        BeanUtils.copyProperties(dishDTO, dish);
        //向菜品表插入1条数据
        dishMapper.insert(dish);
        //获取当前菜品id
        Long dishId = dish.getId();
        //获取口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0){
            //遍历并设置当前菜品id
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}

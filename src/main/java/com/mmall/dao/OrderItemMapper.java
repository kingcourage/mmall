package com.mmall.dao;

import com.mmall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    List<OrderItem> getByOrderNo(@Param(value = "orderNo") Long orderNo);

    List<OrderItem> getByOrderNoUserId(@Param(value = "orderNo") Long orderNo, @Param(value = "userId") Integer userId);

    void batchInsert(@Param("orderItemList")List<OrderItem> orderItemList);
}
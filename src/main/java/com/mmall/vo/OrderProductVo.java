package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author wcy
 * 2018/1/17
 */
public class OrderProductVo {
    private List<OrderItemVo> orderItemVoList;
    private BigDecimal productTotalprice;
    private String imageHost;

    public List<OrderItemVo> getOrderItemVoList() {
        return orderItemVoList;
    }

    public void setOrderItemVoList(List<OrderItemVo> orderItemVoList) {
        this.orderItemVoList = orderItemVoList;
    }

    public BigDecimal getProductTotalprice() {
        return productTotalprice;
    }

    public void setProductTotalprice(BigDecimal productTotalprice) {
        this.productTotalprice = productTotalprice;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}

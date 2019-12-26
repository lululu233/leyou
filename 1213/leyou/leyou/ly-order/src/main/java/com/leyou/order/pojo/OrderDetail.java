package com.leyou.order.pojo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_order_detail")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;// 订单id

    private Long skuId;// 商品id

    private Integer num;// 商品购买数量

    private String title;// 商品标题

    private Long price;// 商品单价

    private String ownSpec;// 商品规格数据

    private String image;// 图片

    private Integer categoryId1;
    private Integer categoryId2;
    private Integer categoryId3;
    private Integer money;

    public Integer getCategoryId1() {
        return categoryId1;
    }

    public void setCategoryId1(Integer categoryId1) {
        this.categoryId1 = categoryId1;
    }

    public Integer getCategoryId2() {
        return categoryId2;
    }

    public void setCategoryId2(Integer categoryId2) {
        this.categoryId2 = categoryId2;
    }

    public Integer getCategoryId3() {
        return categoryId3;
    }

    public void setCategoryId3(Integer categoryId3) {
        this.categoryId3 = categoryId3;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getOwnSpec() {
        return ownSpec;
    }

    public void setOwnSpec(String ownSpec) {
        this.ownSpec = ownSpec;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

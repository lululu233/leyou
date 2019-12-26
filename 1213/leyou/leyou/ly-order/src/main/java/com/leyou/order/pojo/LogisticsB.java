package com.leyou.order.pojo;




import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "tb_logistics_b")
public class LogisticsB {

    @Id
    private Long shippingCode;

    private String shippingName;

    private String shippingAddress;

    private String deliveryDetail;

    private Integer state;

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getDeliveryDetail() {
        return deliveryDetail;
    }

    public void setDeliveryDetail(String deliveryDetail) {
        this.deliveryDetail = deliveryDetail;
    }

    public Long getShippingCode() {
        return shippingCode;
    }

    public void setShippingCode(Long shippingCode) {
        this.shippingCode = shippingCode;
    }

    public String getShippingName() {
        return shippingName;
    }

    public void setShippingName(String shippingName) {
        this.shippingName = shippingName;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}

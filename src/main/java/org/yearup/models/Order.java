package org.yearup.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int orderId;
    private int userId;

    private BigDecimal total;          // use BigDecimal for money
    private BigDecimal shippingAmount; // optional, but useful

    private LocalDateTime createdAt;

    private String address;
    private String city;
    private String state;
    private String zip;

    private List<OrderLineItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(int orderId, BigDecimal total) {
        this.orderId = orderId;
        this.total = total;
        this.createdAt = LocalDateTime.now();
    }

    // --------------------
    // Getters
    // --------------------
    public int getOrderId() {
        return orderId;
    }

    public int getUserId() {
        return userId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public List<OrderLineItem> getItems() {
        return items;
    }

    // --------------------
    // Setters
    // --------------------
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setShippingAmount(BigDecimal shippingAmount) {
        this.shippingAmount = shippingAmount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public void setDate(LocalDateTime date) {
        this.createdAt = date;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void setItems(List<OrderLineItem> items) {
        this.items = items;
    }
}

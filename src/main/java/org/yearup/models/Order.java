package org.yearup.models;

import java.time.LocalDateTime;


public class Order {

    private int orderId;
    private double total;
    private LocalDateTime createdAt;

    public Order() {
        this.createdAt = LocalDateTime.now();
    }

    public Order(int orderId, double total) {
        this.orderId = orderId;
        this.total = total;
        this.createdAt = LocalDateTime.now();
    }

    public int getOrderId() {
        return orderId;
    }

    public double getTotal() {
        return total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}



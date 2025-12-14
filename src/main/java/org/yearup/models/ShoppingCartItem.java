package org.yearup.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;

public class ShoppingCartItem
{
    private Product product = null;
    private int quantity = 1;
    private BigDecimal discountPercent = BigDecimal.ZERO;
    private BigDecimal lineTotal = null; // FIX: store lineTotal when set

    public Product getProduct()
    {
        return product;
    }

    public void setProduct(Product product)
    {
        this.product = product;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public BigDecimal getDiscountPercent()
    {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent)
    {
        this.discountPercent = discountPercent;
    }

    @JsonIgnore
    public int getProductId()
    {
        return this.product.getProductId();
    }

    public BigDecimal getLineTotal()
    {
        // FIX: if lineTotal was explicitly set (from DAO), return it
        if (lineTotal != null)
            return lineTotal;

        // otherwise compute it
        BigDecimal basePrice = product.getPrice();
        BigDecimal qty = new BigDecimal(this.quantity);

        BigDecimal subTotal = basePrice.multiply(qty);
        BigDecimal discountAmount = subTotal.multiply(discountPercent);

        return subTotal.subtract(discountAmount);
    }

    public void setLineTotal(BigDecimal lineTotal)
    {
        // FIX: actually store the lineTotal
        this.lineTotal = lineTotal;
    }
}

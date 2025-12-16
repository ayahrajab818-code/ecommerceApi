package org.yearup.models;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCart
{
    private Map<Integer, ShoppingCartItem> items = new HashMap<>();
    private BigDecimal total = BigDecimal.ZERO; // FIX: store total

    public Map<Integer, ShoppingCartItem> getItems()
    {
        return items;
    }

    public void setItems(Map<Integer, ShoppingCartItem> items)
    {
        this.items = items;
    }

    public boolean contains(int productId)
    {
        return items.containsKey(productId);
    }

    public void add(ShoppingCartItem item)
    {
        items.put(item.getProductId(), item);
    }

    public ShoppingCartItem get(int productId)
    {
        return items.get(productId);
    }

    public BigDecimal getTotal()
    {
        // FIX: return stored total if set, otherwise compute dynamically
        if (total != null)
            return total;

        return items.values()
                .stream()
                .map(ShoppingCartItem::getLineTotal)
                .reduce( BigDecimal.ZERO, (lineTotal, subTotal) -> subTotal.add(lineTotal));
    }

    public void setTotal(BigDecimal total)
    {
        // FIX: actually store the total
        this.total = total;
    }
}

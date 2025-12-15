package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface OrdersDao {
    void createOrderFromCart(int userId, ShoppingCart cart);
}

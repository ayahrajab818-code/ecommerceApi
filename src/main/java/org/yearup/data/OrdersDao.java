package org.yearup.data;

import org.yearup.models.Order;
import org.yearup.models.ShoppingCart;

import java.util.List;

public interface OrdersDao {
    Order createOrderFromCart(int userId, ShoppingCart cart);
    // Order history (all orders for the logged-in user)
    List<Order> getOrdersByUserId(int userId);

    // Order details (one order + its line items, only if it belongs to the user)
    Order getOrderDetails(int userId, int orderId);

}

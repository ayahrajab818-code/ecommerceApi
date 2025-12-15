package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao {
    ShoppingCart getCartByUserId(int userId);
    void addOrIncrement(int userId, int productId);
    void updateQuantityIfExists(int userId, int productId, int quantity);
    void clearCart(int userId);


}


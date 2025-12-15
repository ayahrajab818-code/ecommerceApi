package org.yearup.data.mysql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.yearup.data.OrdersDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

@Component
public class MySqlOrdersDao implements OrdersDao {

    private final JdbcTemplate jdbc;

    public MySqlOrdersDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void createOrderFromCart(int userId, ShoppingCart cart) {

        jdbc.update("INSERT INTO orders (user_id, order_date) VALUES (?, NOW())", userId);

        Integer orderId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);

        for (ShoppingCartItem item : cart.getItems().values()) {
            jdbc.update(
                    "INSERT INTO order_line_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)",
                    orderId,
                    item.getProduct().getProductId(),
                    item.getQuantity(),
                    item.getProduct().getPrice()
            );
        }
    }
}


package org.yearup.data.mysql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class MySqlShoppingCartDao implements ShoppingCartDao {

    private final JdbcTemplate jdbc;

    public MySqlShoppingCartDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // GET cart
    @Override
    public ShoppingCart getCartByUserId(int userId) {

        String sql = """
    SELECT sc.product_id,
           sc.quantity,
           p.product_id,
           p.name,
           p.price,
           p.category_id,
           p.description,
           p.subcategory,
           p.stock,
           p.image_url,
           p.featured
    FROM shopping_cart sc
    JOIN products p ON p.product_id = sc.product_id
    WHERE sc.user_id = ?
""";


        Map<Integer, ShoppingCartItem> items = new LinkedHashMap<>();

        jdbc.query(sql, rs -> {

            int productId = rs.getInt("product_id");
            int quantity = rs.getInt("quantity");

            Product product = new Product(
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getBigDecimal("price"),
                    rs.getInt("category_id"),
                    rs.getString("description"),
                    rs.getString("subcategory"),
                    rs.getInt("stock"),
                    rs.getBoolean("featured"),
                    rs.getString("image_url")
            );

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

            ShoppingCartItem item = new ShoppingCartItem();
            item.setProduct(product);
            item.setQuantity(quantity);


            item.setDiscountPercent(BigDecimal.ZERO);

            item.setLineTotal(lineTotal);

            items.put(productId, item);

        }, userId);

        BigDecimal total = items.values().stream()
                .map(ShoppingCartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ShoppingCart cart = new ShoppingCart();
        cart.setItems(items);
        cart.setTotal(total);

        return cart;
    }

    // POST add product (or increment)
    @Override
    public void addOrIncrement(int userId, int productId) {

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM shopping_cart WHERE user_id = ? AND product_id = ?",
                Integer.class,
                userId,
                productId
        );

        if (count != null && count > 0) {
            jdbc.update(
                    "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?",
                    userId, productId
            );
        } else {
            jdbc.update(
                    "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, 1)",
                    userId, productId
            );
        }
    }

    @Override
    public void updateQuantityIfExists(int userId, int productId, int quantity) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM shopping_cart WHERE user_id=? AND product_id=?",
                Integer.class, userId, productId
        );

        if (count != null && count > 0) {
            jdbc.update(
                    "UPDATE shopping_cart SET quantity=? WHERE user_id=? AND product_id=?",
                    quantity, userId, productId
            );
        }
    }

    @Override
    public void clearCart(int userId) {

    }
}

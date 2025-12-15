package org.yearup.data.mysql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


@Repository
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        String sql = """
                    SELECT sc.product_id, sc.quantity,
                           p.product_id, p.name, p.price, p.category_id, p.description, p.subcategory, p.stock, p.featured, p.image_url
                    FROM shopping_cart sc
                    JOIN products p ON p.product_id = sc.product_id
                    WHERE sc.user_id = ?
                """;

        ShoppingCart cart = new ShoppingCart();

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);

            Map<Integer, ShoppingCartItem> items = new HashMap<>();
            BigDecimal total = BigDecimal.ZERO;

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product product = mapProduct(rs);
                    int quantity = rs.getInt("quantity");

                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setProduct(product);
                    item.setQuantity(quantity);


                    item.setDiscountPercent(BigDecimal.ZERO);

                    BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                    item.setLineTotal(lineTotal);

                    items.put(product.getProductId(), item);
                    total = total.add(lineTotal);
                }
            }
            cart.setItems(items);
            cart.setTotal(total);
            return cart;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void addProduct(int userId, int productId) {
        String selectSql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        String insertSql = "INSERT INTO shopping_cart(user_id, product_id, quantity) VALUES (?, ?, 1)";
        String updateSql = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setInt(1, userId);
            select.setInt(2, productId);

            try (ResultSet rs = select.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                        update.setInt(1, userId);
                        update.setInt(2, productId);
                        update.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insert = connection.prepareStatement(insertSql)) {
                        insert.setInt(1, userId);
                        insert.setInt(2, productId);
                        insert.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProduct(int userId, int productId, int quantity) {

        // FIX: implement PUT behavior (PDF labels PUT optional/bonus) :contentReference[oaicite:3]{index=3}
        // Only update if item already exists; do nothing otherwise.
        if (quantity == 0)
        {
            String deleteSql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?";
            try (Connection connection = getConnection();
                 PreparedStatement ps = connection.prepareStatement(deleteSql))
            {
                ps.setInt(1, userId);
                ps.setInt(2, productId);
                ps.executeUpdate();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
            return;
        }
        String updateSql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(updateSql))
        {
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearCart(int userId) {
        {
            String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
            try (Connection connection = getConnection();
                 PreparedStatement ps = connection.prepareStatement(sql))
            {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
        // FIX: implement DELETE cart: delete all items for user
//        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
//        try (Connection connection = getConnection();
//             PreparedStatement ps = connection.prepareStatement(sql)) {
//
//            ps.setInt(1, userId);
//            ps.executeUpdate();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }

    // FIX: local mapper so this class does not depend on ProductDao
    private static Product mapProduct(ResultSet rs) throws SQLException {
        int productId = rs.getInt("product_id");
        String name = rs.getString("name");
        BigDecimal price = rs.getBigDecimal("price");
        int categoryId = rs.getInt("category_id");
        String description = rs.getString("description");
        String subCategory = rs.getString("subcategory");
        int stock = rs.getInt("stock");
        boolean featured = rs.getBoolean("featured");
        String imageUrl = rs.getString("image_url");
        return new Product(productId, name, price, categoryId, description, subCategory, stock, featured, imageUrl);
    }


    @Override
    public void removeProduct(int userId, int productId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

}

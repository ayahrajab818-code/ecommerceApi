package org.yearup.data.mysql;

import org.springframework.stereotype.Repository;
import org.yearup.data.OrdersDao;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MySqlOrdersDao implements OrdersDao {

    private final DataSource dataSource;

    public MySqlOrdersDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ---------------------------
    // CHECKOUT (create order)
    // ---------------------------
    @Override
    public Order createOrderFromCart(int userId, ShoppingCart cart) {
        String insertOrderSql = """
            INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount)
            VALUES (?, NOW(), '', '', '', '', 0.00)
        """;

        String insertLineSql = """
            INSERT INTO order_line_items (order_id, product_id, sales_price, quantity, discount)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            int orderId;

            // 1) Insert order
            try (PreparedStatement ps = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        throw new RuntimeException("Failed to create order (no generated key).");
                    }
                    orderId = keys.getInt(1);
                }
            }

            // 2) Insert line items from cart
            try (PreparedStatement ps = conn.prepareStatement(insertLineSql)) {
                for (ShoppingCartItem item : cart.getItems().values()) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getProduct().getProductId());
                    ps.setBigDecimal(3, item.getProduct().getPrice());
                    ps.setInt(4, item.getQuantity());
                    ps.setBigDecimal(5, BigDecimal.ZERO);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();

            // 3) Return order details (includes items + total)
            return getOrderDetails(userId, orderId);

        } catch (SQLException e) {
            throw new RuntimeException("Error creating order: " + e.getMessage(), e);
        }
    }

    // ---------------------------
    // ORDER HISTORY
    // ---------------------------
    @Override
    public List<Order> getOrdersByUserId(int userId) {

        // NOTE: alias order_id as orderId to match mapping
        String sql = """
            SELECT
                o.order_id AS orderId,
                o.user_id  AS userId,
                o.date     AS createdAt,
                o.address,
                o.city,
                o.state,
                o.zip,
                o.shipping_amount AS shippingAmount,
                (
                    SELECT COALESCE(SUM((oli.sales_price * oli.quantity) - oli.discount), 0)
                    FROM order_line_items oli
                    WHERE oli.order_id = o.order_id
                ) AS total
            FROM orders o
            WHERE o.user_id = ?
            ORDER BY o.order_id DESC
        """;

        List<Order> orders = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrder(rs, false));
                }
            }

            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("Error loading orders: " + e.getMessage(), e);
        }
    }

    // ---------------------------
    // ORDER DETAILS (with items)
    // ---------------------------
    @Override
    public Order getOrderDetails(int userId, int orderId) {

        String orderSql = """
            SELECT
                o.order_id AS orderId,
                o.user_id  AS userId,
                o.date     AS createdAt,
                o.address,
                o.city,
                o.state,
                o.zip,
                o.shipping_amount AS shippingAmount,
                (
                    SELECT COALESCE(SUM((oli.sales_price * oli.quantity) - oli.discount), 0)
                    FROM order_line_items oli
                    WHERE oli.order_id = o.order_id
                ) AS total
            FROM orders o
            WHERE o.user_id = ? AND o.order_id = ?
        """;

        String itemsSql = """
            SELECT
                order_line_item_id AS orderLineItemId,
                order_id           AS orderId,
                product_id         AS productId,
                sales_price        AS salesPrice,
                quantity,
                discount
            FROM order_line_items
            WHERE order_id = ?
            ORDER BY order_line_item_id
        """;

        try (Connection conn = dataSource.getConnection()) {

            // 1) Load the order
            Order order = null;
            try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, orderId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        order = mapOrder(rs, true);
                    }
                }
            }

            if (order == null) return null;

            // 2) Load items
            List<OrderLineItem> items = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(itemsSql)) {
                ps.setInt(1, orderId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        OrderLineItem item = new OrderLineItem();
                        item.setOrderLineItemId(rs.getInt("orderLineItemId"));
                        item.setOrderId(rs.getInt("orderId"));
                        item.setProductId(rs.getInt("productId"));
                        item.setSalesPrice(rs.getBigDecimal("salesPrice"));
                        item.setQuantity(rs.getInt("quantity"));
                        item.setDiscount(rs.getBigDecimal("discount"));
                        items.add(item);
                    }
                }
            }

            order.setItems(items);
            return order;

        } catch (SQLException e) {
            throw new RuntimeException("Error loading order details: " + e.getMessage(), e);
        }
    }

    // ---------------------------
    // Helper: map order row
    // ---------------------------
    private Order mapOrder(ResultSet rs, boolean includeItemsField) throws SQLException {
        Order o = new Order();

        // IMPORTANT: these column names match the SQL aliases above
        o.setOrderId(rs.getInt("orderId"));
        o.setUserId(rs.getInt("userId"));

        Timestamp ts = rs.getTimestamp("createdAt");
        if (ts != null) o.setCreatedAt(ts.toLocalDateTime());

        o.setAddress(rs.getString("address"));
        o.setCity(rs.getString("city"));
        o.setState(rs.getString("state"));
        o.setZip(rs.getString("zip"));

        o.setShippingAmount(rs.getBigDecimal("shippingAmount"));
        o.setTotal(rs.getBigDecimal("total"));

        if (includeItemsField) {
            o.setItems(new ArrayList<>());
        }

        return o;
    }
}

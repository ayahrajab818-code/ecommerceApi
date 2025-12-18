package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrdersDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.security.Principal;
import java.util.List;

// Marks this class as a REST controller (returns JSON responses)
@RestController

// All routes in this controller start with /orders
@RequestMapping("/orders")

// Allows the front-end (different origin/port) to call these endpoints
@CrossOrigin
public class OrdersController {

    // DAO for reading and clearing the shopping cart
    private final ShoppingCartDao cartDao;

    // DAO for finding the current user by username
    private final UserDao userDao;

    // DAO for creating orders and fetching order history/details
    private final OrdersDao ordersDao;

    // Constructor injection: Spring supplies the correct DAO implementations
    public OrdersController(
            // FIXED: Forces Spring to inject the MySQL shopping cart DAO when multiple ShoppingCartDao beans exist
            @Qualifier("mySqlShoppingCartDao") ShoppingCartDao cartDao,
            UserDao userDao,
            OrdersDao ordersDao
    ) {
        this.cartDao = cartDao;
        this.userDao = userDao;
        this.ordersDao = ordersDao;
    }

    // GET /orders
    // Returns the order history for the currently logged-in user
    @GetMapping
    public List<Order> getMyOrders(Principal principal) {

        // Principal contains the authenticated username from the JWT token
        String username = principal.getName();

        // Look up the user in the database
        User user = userDao.getByUserName(username);

        // FIXED: If user doesn't exist, return 401 instead of failing or returning empty data
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.");
        }

        // Return all orders for this user
        return ordersDao.getOrdersByUserId(user.getId());
    }

    // GET /orders/{orderId}
    // Returns full details for a single order (including line items)
    @GetMapping("/{orderId}")
    public Order getOrderDetails(@PathVariable int orderId, Principal principal) {

        // Get the authenticated username
        String username = principal.getName();

        // Look up the user in the database
        User user = userDao.getByUserName(username);

        // FIXED: Return 401 if the token is valid but the user no longer exists in the DB
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.");
        }

        // Load the order details, scoped to this userId so users cannot read others' orders
        Order order = ordersDao.getOrderDetails(user.getId(), orderId);

        // FIXED: If the order doesn't exist (or doesn't belong to this user), return 404
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found.");
        }

        // Return the order details as JSON
        return order;
    }

    // POST /orders
    // Creates a new order from the current user's cart (checkout)
    @PostMapping

    // FIXED: Returns 201 Created when checkout succeeds
    @ResponseStatus(HttpStatus.CREATED)
    public Order checkout(Principal principal) {

        // 1) Identify the logged-in user from the token
        String username = principal.getName();
        User user = userDao.getByUserName(username);

        // FIXED: If user cannot be found, return 401 Unauthorized
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found.");
        }

        int userId = user.getId();

        // 2) Load the user's shopping cart from the database
        ShoppingCart cart = cartDao.getByUserId(userId);

        // FIXED: Prevent checkout if cart is empty, return 400 Bad Request
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty.");
        }

        // 3) Create the order in the database (and insert order line items)
        Order created = ordersDao.createOrderFromCart(userId, cart);

        // 4) Clear the cart so it is empty after checkout
        cartDao.clearCart(userId);

        // Return the created order (can include id, totals, items depending on DAO)
        return created;
    }
}

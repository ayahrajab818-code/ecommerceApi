package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.OrdersDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersDao ordersDao;
    private final ShoppingCartDao cartDao;
    private final UserDao userDao;

    public OrdersController(OrdersDao ordersDao, ShoppingCartDao cartDao, UserDao userDao) {
        this.ordersDao = ordersDao;
        this.cartDao = cartDao;
        this.userDao = userDao;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkout() {
        int userId = getCurrentUserId();

        ShoppingCart cart = cartDao.getCartByUserId(userId);
        ordersDao.createOrderFromCart(userId, cart);

        cartDao.clearCart(userId);
    }

    private int getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userDao.getByUserName(auth.getName());
        if (user == null) throw new RuntimeException("User not found");
        return user.getId();
    }
}


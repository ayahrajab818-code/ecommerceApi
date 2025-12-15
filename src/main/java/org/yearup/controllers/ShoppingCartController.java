package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.util.Map;
@CrossOrigin(origins = "http://localhost:63342")
@RestController
@RequestMapping("/cart")
public class ShoppingCartController {

    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;

    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
    }

    // GET http://localhost:8080/cart
    @GetMapping
    public ShoppingCart getCart() {
        int userId = getCurrentUserId();
        return shoppingCartDao.getCartByUserId(userId);
    }

    // POST http://localhost:8080/cart/products/15
    @PostMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addToCart(@PathVariable int productId) {
        int userId = getCurrentUserId();
        shoppingCartDao.addOrIncrement(userId, productId);
    }

    // PUT http://localhost:8080/cart/products/15   body: {"quantity": 3}
    @PutMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCartItem(@PathVariable int productId,
                               @RequestBody Map<String, Integer> body) {

        Integer qty = body.get("quantity");
        if (qty == null) {
            throw new IllegalArgumentException("quantity is required");
        }

        int userId = getCurrentUserId();
        shoppingCartDao.updateQuantityIfExists(userId, productId, qty);
    }

    // DELETE http://localhost:8080/cart
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart() {
        int userId = getCurrentUserId();
        shoppingCartDao.clearCart(userId);
    }

    private int getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.getByUserName(username);

        if (user == null) {
            // This prevents “mystery 500s” and tells you it’s an auth/user lookup issue
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found: " + username);
        }

        return user.getId();
    }
}






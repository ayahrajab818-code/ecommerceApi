package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

// Marks this class as a REST controller (returns JSON responses)
@RestController

// All endpoints in this controller start with /cart
@RequestMapping("/cart")

// Allows the front-end (different origin/port) to call these endpoints
@CrossOrigin

// FIXED: Requires the user to be logged in for every endpoint in this controller
// This prevents unauthenticated users from reading or modifying carts
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController {

    // DAO used for cart operations (get cart, add product, update quantity, clear cart, remove product)
    private final ShoppingCartDao shoppingCartDao;

    // DAO used to find the logged-in user record so we can get userId
    private final UserDao userDao;

    // DAO used to validate that a product exists before adding/updating/removing
    private final ProductDao productDao;

    // Constructor injection: Spring provides DAO implementations
    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // GET /cart
    // Returns the cart for the currently logged-in user
    @GetMapping("")
    public ShoppingCart getCart(Principal principal) {
        try {
            // Principal holds the username of the authenticated user (from JWT)
            String userName = principal.getName();

            // Look up the user in the database
            User user = userDao.getByUserName(userName);

            // FIXED: If user record is missing, return 401 Unauthorized
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            // Use userId to load the cart
            int userId = user.getId();

            // Return the cart from the DAO
            return shoppingCartDao.getByUserId(userId);
        }
        catch (ResponseStatusException ex) {
            // Preserve intended HTTP errors (401, 404, 400, etc.)
            throw ex;
        }
        catch (Exception ex) {
            // FIXED: Return 500 for unexpected server errors
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // POST /cart/products/{productId}
    // Adds a product to the user's cart
    // Capstone behavior: adding the same product typically increases quantity by 1
    @PostMapping("/products/{productId}")
    public ShoppingCart addProductToCart(@PathVariable int productId, Principal principal) {
        try {
            // Get the logged-in username
            String userName = principal.getName();

            // Look up user in DB
            User user = userDao.getByUserName(userName);

            // FIXED: Return 401 if user not found
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            // Get userId
            int userId = user.getId();

            // FIXED: Validate the product exists before adding to cart
            if (productDao.getById(productId) == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            // Add product to cart using DAO logic
            shoppingCartDao.addProduct(userId, productId);

            // Return the updated cart so the front end can refresh UI
            return shoppingCartDao.getByUserId(userId);
        }
        catch (ResponseStatusException ex) {
            // Preserve intended status codes (404, 401, etc.)
            throw ex;
        }
        catch (Exception ex) {
            // FIXED: Return 500 for unexpected errors
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // PUT /cart/products/{productId}
    // Updates the quantity of a product in the user's cart
    // Request body: ShoppingCartItem (only quantity is used)
    @PutMapping("/products/{productId}")
    public ShoppingCart updateProductInCart(
            @PathVariable int productId,
            @RequestBody ShoppingCartItem item,
            Principal principal
    ) {
        try {
            // Get logged-in username
            String userName = principal.getName();

            // Look up the user in DB
            User user = userDao.getByUserName(userName);

            // FIXED: Return 401 if user not found
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            // Extract userId
            int userId = user.getId();

            // FIXED: Validate product exists before updating
            if (productDao.getById(productId) == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            // FIXED: Validate request body and quantity
            // Quantity must be 0 or more (0 can be used as "remove")
            if (item == null || item.getQuantity() < 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be 0 or greater.");

            // Update the quantity in the cart
            shoppingCartDao.updateProduct(userId, productId, item.getQuantity());

            // Return updated cart
            return shoppingCartDao.getByUserId(userId);
        }
        catch (ResponseStatusException ex) {
            // Preserve intended HTTP errors (400, 401, 404, etc.)
            throw ex;
        }
        catch (Exception ex) {
            // FIXED: Return 500 for unexpected errors
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // DELETE /cart
    // Clears all products from the current user's cart
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // FIXED: Returns 204 No Content when cart is cleared successfully
    public void clearCart(Principal principal) {
        try {
            // Get logged-in username
            String userName = principal.getName();

            // Look up user in DB
            User user = userDao.getByUserName(userName);

            // FIXED: Return 401 if user not found
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            // Extract userId
            int userId = user.getId();

            // Clear cart in DB
            shoppingCartDao.clearCart(userId);
        }
        catch (ResponseStatusException ex) {
            // Preserve correct HTTP status codes
            throw ex;
        }
        catch (Exception ex) {
            // FIXED: Return 500 for unexpected errors
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // DELETE /cart/products/{productId}
    // Removes a single product from the user's cart
    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    // FIXED: Uses 204 No Content because delete succeeds with no response body
    public void removeProductFromCart(@PathVariable int productId, Principal principal)
    {
        // FIXED: Uses the principal to ensure we remove from the correct user's cart
        // This prevents users from modifying someone else's cart
        User user = userDao.getByUserName(principal.getName());
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        int userId = user.getId();

        // Remove the product from cart in DB
        shoppingCartDao.removeProduct(userId, productId);
    }
}

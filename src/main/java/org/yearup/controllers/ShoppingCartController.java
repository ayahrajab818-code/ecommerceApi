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

// convert this class to a REST controller
// only logged-in users should have access to these actions
@RestController
@RequestMapping("/cart")
@CrossOrigin // allow cross-site requests (matches other controllers)
@PreAuthorize("isAuthenticated()")


public class ShoppingCartController {
    // a shopping cart requires
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProductDao productDao;

    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    //Each method in this controller requires a Principal object as a parameter
    @GetMapping("")
    public ShoppingCart getCart(Principal principal) {
        try {
            // get the currently logged-in username
            String userName = principal.getName();// find database user by userId
            User user = userDao.getByUserName(userName);

            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            int userId = user.getId();

            // use the shopping-cart-Dao to get all items in the cart and return the cart
            return shoppingCartDao.getByUserId(userId);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("/products/{productId}")
    public ShoppingCart addProductToCart(@PathVariable int productId, Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            int userId = user.getId();

            if (productDao.getById(productId) == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            shoppingCartDao.addProduct(userId, productId); //add item to cart
            return shoppingCartDao.getByUserId(userId);  // return updated cart
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }


    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated

    @PutMapping("/products/{productId}")
    public ShoppingCart updateProductInCart(@PathVariable int productId, @RequestBody ShoppingCartItem item, Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            int userId = user.getId();

            if (productDao.getById(productId) == null) // validate product exists
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            if(item == null || item.getQuantity() < 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be 0 or greater.");

            shoppingCartDao.updateProduct(userId,productId, item.getQuantity());  //update quantity
            return shoppingCartDao.getByUserId(userId);
        }
        catch(ResponseStatusException ex) {
            throw ex;
        }   catch(Exception ex)
        {  throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");  }
    }


    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Principal principal)  {
        try
        {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

            int userId = user.getId();
            shoppingCartDao.clearCart(userId);  //clear all product from cart
        }  catch(ResponseStatusException ex) {
            throw ex;} // preserve correct HTTP status codes
        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void removeProductFromCart(@PathVariable int productId, Principal principal)
    {
        int userId = userDao.getByUserName(principal.getName()).getId();
        shoppingCartDao.removeProduct(userId, productId);
    }

}



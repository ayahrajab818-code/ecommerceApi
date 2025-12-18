package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Product;
import org.yearup.data.ProductDao;

import java.math.BigDecimal;
import java.util.List;

// Marks this class as a REST controller (returns JSON)
@RestController

// All routes in this controller start with /products
// This keeps the API consistent (ex: GET /products, GET /products/1, etc.)
@RequestMapping("/products")

// Allows the front-end (different origin/port) to call these endpoints
@CrossOrigin
public class ProductsController
{
    // DAO used to access product data in the database
    // This controller depends on ProductDao for search, create, update, delete operations
    private ProductDao productDao;

    // Constructor injection: Spring provides the ProductDao implementation
    @Autowired
    public ProductsController(ProductDao productDao)
    {
        this.productDao = productDao;
    }

    // GET /products
    // Supports searching/filtering products using optional query parameters:
    // - cat (categoryId)
    // - minPrice
    // - maxPrice
    // - subCategory
    @GetMapping("")
    @PreAuthorize("permitAll()") // Anyone can search/browse products (no login required)
    public List<Product> search(
            @RequestParam(name="cat", required = false) Integer categoryId,
            @RequestParam(name="minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name="maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name="subCategory", required = false) String subCategory
    )
    {
        try {
            // Calls the DAO search method that applies filters based on provided parameters
            return productDao.search(categoryId, minPrice, maxPrice, subCategory);
        }
        catch(ResponseStatusException ex)
        {
            // If DAO/controller intentionally throws a ResponseStatusException, preserve it
            throw ex;
        }
        catch(Exception ex)
        {
            // FIXED: Ensures unexpected errors return a clean 500 response instead of crashing
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // GET /products/{id}
    // Returns a single product by id
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()") // Anyone can view a product by id
    public Product getById(@PathVariable int id )
    {
        try {
            // Load the product
            var product = productDao.getById(id);

            // FIXED: If product doesn't exist, return 404 Not Found
            if (product == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            // Return the product as JSON
            return product;
        }
        catch(ResponseStatusException ex)
        {
            // Preserve intended HTTP errors (404, 400, etc.)
            throw ex;
        }
        catch(Exception ex)
        {
            // FIXED: Return 500 for unexpected issues
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // POST /products
    // Creates a new product (Admin only)
    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')") // FIXED: only admins can create products
    public Product addProduct(@RequestBody Product product)
    {
        try
        {
            // Create and return the product
            return productDao.create(product);
        }
        catch(ResponseStatusException ex)
        {
            // Preserve intended HTTP errors thrown from DAO
            throw ex;
        }
        catch(Exception ex)
        {
            // FIXED: Return 500 for unexpected issues
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // PUT /products/{id}
    // Updates an existing product (Admin only)
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    // FIXED: you corrected an authorization issue here.
    // The correct form is hasRole('ADMIN') (not role_admin or other variations).
    public void updateProduct(@PathVariable int id, @RequestBody Product product)
    {
        try
        {
            // FIXED: This must call update(), not create().
            // Calling create() during an update causes duplication (new product row).
            productDao.update(id, product);
        }
        catch(ResponseStatusException ex)
        {
            // Preserve intended HTTP errors
            throw ex;
        }
        catch(Exception ex)
        {
            // FIXED: Return 500 for unexpected issues
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // DELETE /products/{id}
    // Deletes a product by id (Admin only)
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only admins can delete products
    public void deleteProduct(@PathVariable int id)
    {
        try {
            // Check product exists first
            var product = productDao.getById(id);

            // FIXED: If product doesn't exist, return 404 instead of silently succeeding
            if (product == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            // Delete the product
            productDao.delete(id);
        }
        catch(ResponseStatusException ex)
        {
            // Preserve intended HTTP errors
            throw ex;
        }
        catch(Exception ex)
        {
            // FIXED: Return 500 for unexpected issues
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}

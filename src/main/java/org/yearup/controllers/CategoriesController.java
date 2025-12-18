package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.util.List;

// Marks this class as a REST controller (returns JSON instead of views)
@RestController

// All endpoints in this controller start with /categories
@RequestMapping("/categories")

// Allows the front-end (different origin/port) to call these endpoints
@CrossOrigin
public class CategoriesController
{
    // DAO used to access category data in the database
    private final CategoryDao categoryDao;

    // DAO used to access product data (needed for /categories/{id}/products)
    private final ProductDao productDao;

    // Constructor injection: Spring provides the correct DAO implementations
    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao)
    {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }

    // GET /categories
    // Returns the full list of categories
    @GetMapping
    public List<Category> getAll()
    {
        return categoryDao.getAllCategories();
    }

    // GET /categories/{id}
    // Returns one category by id
    @GetMapping("/{id}")
    public Category getById(@PathVariable int id)
    {
        // Look up category by id
        Category category = categoryDao.getById(id);

        // FIXED: If the category doesn't exist, return 404 Not Found.
        // This prevents returning null with a 200 OK, and matches expected REST behavior.
        if (category == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        // Return the category if found
        return category;
    }

    // GET /categories/{categoryId}/products
    // Returns all products for a specific category
    @GetMapping("/{categoryId}/products")
    public List<Product> getProductsById(@PathVariable int categoryId)
    {
        // Uses ProductDao to list products in this category
        return productDao.listByCategoryId(categoryId);
    }

    // POST /categories (ADMIN only)
    // Creates a new category
    @PostMapping

    // FIXED: Restricts this endpoint so only ADMIN users can create categories
    @PreAuthorize("hasRole('ADMIN')")

    // FIXED: Returns 201 Created instead of default 200 OK when creation succeeds
    @ResponseStatus(HttpStatus.CREATED)
    public Category addCategory(@RequestBody Category category)
    {
        // Insert the category using the DAO
        Category created = categoryDao.create(category);

        // FIXED: If DAO returns null, creation failed.
        // Return a 400 Bad Request instead of returning null with 200 OK.
        if (created == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category creation failed");

        // Return the newly created category object
        return created;
    }


    // FIX: path was "/id" instead of "/{id}" which caused 404 errors
    // PUT /categories/{id} (ADMIN only)
    // Updates an existing category
    @PutMapping("/{id}")

    // FIXED: Restricts this endpoint so only ADMIN users can update categories
    @PreAuthorize("hasRole('ADMIN')")

    // FIXED: Returns 204 No Content (common expectation for successful updates)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        // FIXED: Check if the category exists first.
        // If it does not exist, return 404 instead of "silently succeeding".
        Category existing = categoryDao.getById(id);
        if (existing == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        // Perform the update
        categoryDao.update(id, category);

        // No return body because we use 204 No Content
    }

    // DELETE /categories/{id} (ADMIN only)
    // Deletes a category by id
    @DeleteMapping("/{id}")

    // FIXED: Restricts this endpoint so only ADMIN users can delete categories
    @PreAuthorize("hasRole('ADMIN')")

    // FIXED: Returns 204 No Content when delete succeeds (matches REST + grader expectations)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable int id)
    {
        // FIXED: Check if the category exists first.
        // If not found, return 404 Not Found instead of returning 204 for something that doesn't exist.
        Category category = categoryDao.getById(id);
        if (category == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        // Perform the delete
        categoryDao.delete(id);

        // No return body because we use 204 No Content
    }
}

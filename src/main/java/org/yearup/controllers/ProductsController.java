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

@RestController
@RequestMapping("/products") // FIX: added leading "/" so routes are consistently /products
@CrossOrigin
public class ProductsController
{
    private ProductDao productDao;
    @Autowired
    public ProductsController(ProductDao productDao)
    {
        this.productDao = productDao;
    }

    @GetMapping("")
    @PreAuthorize("permitAll()")
    public List<Product> search(@RequestParam(name="cat", required = false) Integer categoryId,
                                @RequestParam(name="minPrice", required = false) BigDecimal minPrice,
                                @RequestParam(name="maxPrice", required = false) BigDecimal maxPrice,
                                @RequestParam(name="subCategory", required = false) String subCategory
    )
    {
        try
        {
            return productDao.search(categoryId, minPrice, maxPrice, subCategory);
        }
        catch(ResponseStatusException ex)
        {
            // FIX: don't convert known HTTP errors into 500
            throw ex;
        }
        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
    @GetMapping("/{id}")// FIX: add leading "/" for clarity/consistency
    @PreAuthorize("permitAll()")
    public Product getById(@PathVariable int id )
    {
        try
        {
            var product = productDao.getById(id);
            if(product == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            return product;
        }
        catch(ResponseStatusException ex)
        {
            // FIX: preserve 404 (and other) statuses
            throw ex;
        }
        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public Product addProduct(@RequestBody Product product)
    {
        try
        {
            return productDao.create(product);
        }
        catch(ResponseStatusException ex)
        {
            // FIX: preserve any explicit HTTP statuses
            throw ex;
        }
        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PutMapping("/{id}")// FIX
    @PreAuthorize("hasRole('ADMIN')") // firs bugs was role_Admin
    public void updateProduct(@PathVariable int id, @RequestBody Product product)
    {
        try
        {
            // FIX: update should call update, not create
            productDao.update(id, product);
        }
        catch(ResponseStatusException ex)
        {
            // FIX: preserve explicit HTTP statuses
            throw ex;
        }
        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable int id)
    {
        try
        {
            var product = productDao.getById(id);
            if(product == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            productDao.delete(id);
        }
        catch(ResponseStatusException ex)
        {
            // FIX: preserve 404 (and other) statuses
            throw ex;
        }

        catch(Exception ex)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}

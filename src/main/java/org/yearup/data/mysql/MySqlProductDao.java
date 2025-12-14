package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.yearup.models.Product;
import org.yearup.data.ProductDao;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    @Autowired
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String subCategory)
    {
        List<Product> products = new ArrayList<>();

        // FIX: use minPrice/maxPrice correctly and include >= for minPrice
        // FIX: column name is "subcategory" (not "sub_category") based on your mapRow() usage
        String sql = "SELECT * FROM products " +
                " WHERE (category_id = ? OR ? = -1) " +
                " AND (price >= ? OR ? = -1) " +
                " AND (price <= ? OR ? = -1) " +
                " AND (subcategory = ? OR ? = '') ";


        categoryId = categoryId == null ? -1 : categoryId;
        minPrice = minPrice == null ? new BigDecimal("-1") : minPrice;
        maxPrice = maxPrice == null ? new BigDecimal("-1") : maxPrice;
        subCategory = subCategory == null ? "" : subCategory;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);
            statement.setInt(2, categoryId);

            statement.setBigDecimal(3, minPrice);
            statement.setBigDecimal(4, minPrice);

            statement.setBigDecimal(5, maxPrice);   // FIX: use maxPrice (was minPrice)
            statement.setBigDecimal(6, maxPrice);   // FIX: use maxPrice (was minPrice)

            statement.setString(7, subCategory);
            statement.setString(8, subCategory);

            try (ResultSet row = statement.executeQuery()) // FIX: close ResultSet
            {

                while (row.next())
                {
                    products.add(mapRow(row));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return products;
    }

    @Override
    public List<Product> listByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products " +
                " WHERE category_id = ? ";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);

            try (ResultSet row = statement.executeQuery()) // FIX: close ResultSet
            {
                while (row.next()) {
                    products.add(mapRow(row));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return products;
    }


    @Override
    public Product getById(int productId)
    {
        String sql = "SELECT * FROM products WHERE product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, productId);

            try (ResultSet row = statement.executeQuery()) // FIX: close ResultSet
            {
                if (row.next())
                {
                    return mapRow(row);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public Product create(Product product)
    {

        // FIX: column name should be "subcategory" (consistent with mapRow and UPDATE)
        String sql = "INSERT INTO products(name, price, category_id, description, subcategory, image_url, stock, featured) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) // FIX: Statement constant
        {
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());

            int rowsAffected = statement.executeUpdate();

            // FIX: don’t return null silently if insert fails
            if (rowsAffected == 0)
                throw new RuntimeException("Creating product failed, no rows affected."); // Retrieve the generated keys
            // FIX: close generatedKeys and use correct variable names (productId, not orderId)
            try (ResultSet generatedKeys = statement.getGeneratedKeys())
            {
                if (generatedKeys.next())
                {
                    int newProductId = generatedKeys.getInt(1);
                    return getById(newProductId); // FIX: return the newly inserted product
                }
                else
                {
                    throw new RuntimeException("Creating product failed, no ID obtained.");
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(int productId, Product product)
    {
        // FIX: add space after "products" so SQL is valid
        String sql = "UPDATE products " +
                " SET name = ? " +
                "   , price = ? " +
                "   , category_id = ? " +
                "   , description = ? " +
                "   , subcategory = ? " +
                "   , image_url = ? " +
                "   , stock = ? " +
                "   , featured = ? " +
                " WHERE product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());
            statement.setInt(9, productId);

            int rowsAffected = statement.executeUpdate();

            // FIX: ensure an actual row was updated
            if (rowsAffected == 0)
                throw new RuntimeException("Product not found for update");
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int productId)
    {

        String sql = "DELETE FROM products " +
                " WHERE product_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, productId);

            int rowsAffected = statement.executeUpdate();

            // FIX: ensure an actual row was deleted
            if (rowsAffected == 0)
                throw new RuntimeException("Product not found for delete");
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected static Product mapRow(ResultSet row) throws SQLException
    {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String subCategory = row.getString("subcategory");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");
        return new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl);
    }
}

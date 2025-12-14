package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository // FIX: was @Component (still works, but Repository is correct for DAOs)
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    @Autowired
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories() // get all categories
    { List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories";

        // FIX: close statement/resultset with try-with-resources to avoid leaks
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet row = statement.executeQuery())
        {

            while (row.next())
            {
                Category category = mapRow(row);
                categories.add(category);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return categories;
    }

    @Override
    public Category getById(int categoryId) // get category by id
    {
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);

            try (ResultSet row = statement.executeQuery())
            {
                if (row.next())
                {
                    return mapRow(row); // FIX: return the category if found
                }
                else
                {
                    return null; // FIX: explicitly return null if category does not exist
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Category create(Category category) // create a new category
    {
        String sql = "INSERT INTO categories(name, description) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            int rowsAffected = statement.executeUpdate();

            // FIX: if nothing inserted, fail (don’t return null silently)
            if (rowsAffected == 0)
            {
                throw new RuntimeException("Creating category failed, no rows affected.");
            }

            // FIX: return the created category using the generated id
            try (ResultSet generatedKeys = statement.getGeneratedKeys())
            {
                if (generatedKeys.next())
                {
                    int newCategoryId = generatedKeys.getInt(1);
                    return getById(newCategoryId);
                }
                else
                {
                    throw new RuntimeException("Creating category failed, no ID obtained.");
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void update(int categoryId, Category category)
    {
        String sql = "UPDATE categories " +
                " SET name = ? " +
                "   , description = ? " +
                " WHERE category_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);
            int rowsAffected = statement.executeUpdate();
            // FIX: ensure a category was  updated
            if (rowsAffected == 0)
            {
                throw new RuntimeException("Category not found for update");
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        } // update category
    }

    @Override
    public void delete(int categoryId)  // delete category
    { String sql = "DELETE FROM categories " +
            " WHERE category_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        // FIX: remove double-brace initialization (creates anonymous inner class)
        Category category = new Category();
        category.setCategoryId(categoryId);
        category.setName(name);
        category.setDescription(description);

        return category;
    }

}

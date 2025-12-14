package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.yearup.data.UserDao;
import org.yearup.models.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository // FIX: was @Component
public class MySqlUserDao extends MySqlDaoBase implements UserDao
{
    private final BCryptPasswordEncoder passwordEncoder; // FIX: reuse encoder instead of new each time

    @Autowired
    public MySqlUserDao(DataSource dataSource)
    {
        super(dataSource);
        this.passwordEncoder = new BCryptPasswordEncoder(); // FIX
    }

    @Override
    public User create(User newUser)
    {
        String sql = "INSERT INTO users (username, hashed_password, role) VALUES (?, ?, ?)";
        String hashedPassword = passwordEncoder.encode(newUser.getPassword()); // FIX: reuse encoder

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) // FIX: close PS
        {
            ps.setString(1, newUser.getUsername());
            ps.setString(2, hashedPassword);
            ps.setString(3, newUser.getRole());

            int rowsAffected = ps.executeUpdate();

            // FIX: ensure insert happened
            if (rowsAffected == 0)
                throw new RuntimeException("Creating user failed, no rows affected.");

            User user = getByUserName(newUser.getUsername());
            if (user == null)
                throw new RuntimeException("Creating user failed, could not load created user.");

            user.setPassword(""); // keep current behavior
            return user;
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> getAll()
    {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet row = statement.executeQuery()) // FIX: close ResultSet
        {
            while (row.next())
            {
                users.add(mapRow(row));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return users;
    }

    @Override
    public User getUserById(int id)
    {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) // FIX: close statement
        {
            statement.setInt(1, id);

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
    public User getByUserName(String username)
    {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) // FIX: close statement
        {
            statement.setString(1, username);

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
    public int getIdByUsername(String username)
    {
        User user = getByUserName(username);
        return user != null ? user.getId() : -1;
    }

    @Override
    public boolean exists(String username)
    {
        return getByUserName(username) != null;
    }

    private User mapRow(ResultSet row) throws SQLException
    {
        int userId = row.getInt("user_id");
        String username = row.getString("username");
        String hashedPassword = row.getString("hashed_password");
        String role = row.getString("role");

        return new User(userId, username, hashedPassword, role);
    }
}

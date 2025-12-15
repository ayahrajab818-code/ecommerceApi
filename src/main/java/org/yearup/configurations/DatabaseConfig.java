package org.yearup.configurations;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yearup.data.*;
import org.yearup.data.mysql.*;

@Configuration
public class DatabaseConfig
{
    private BasicDataSource basicDataSource;

    @Bean
    public BasicDataSource dataSource() {
        return basicDataSource;
    }

    @Bean
    public ShoppingCartDao shoppingCartDao() //register ShoppingCartDao as a Spring bean so ShoppingCartController can be created
    {
        return new MySqlShoppingCartDao(basicDataSource);
    }
    @Bean
    public CategoryDao categoryDao()
    {
        // Used the existing MySqlCategoriesDao implementation (no new DAO created)
        return new MySqlCategoryDao(basicDataSource);
    }
    //Constructor that initializes the BasicDataSource with database credentials
    @Autowired
    public DatabaseConfig(@Value("${datasource.url}") String url,
                          @Value("${datasource.username}") String username,
                          @Value("${datasource.password}") String password)
    {
        basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
    }
    @Bean
    public ProductDao productDao()
    {
        return new MySqlProductDao(basicDataSource);
    }

    @Bean
    public UserDao userDao()
    {
        return new MySqlUserDao(basicDataSource);
    }

    @Bean
    public ProfileDao profileDao()
    {
        return new MySqlProfileDao(basicDataSource);
    }


}
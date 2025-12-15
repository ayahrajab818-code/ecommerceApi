package org. yearup.configurations;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.yearup.data.CategoryDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.mysql.MySqlCategoryDao;
import org.yearup.data.mysql.MySqlShoppingCartDao;

@Configuration
public class DatabaseConfig {

<<<<<<< HEAD
    private final BasicDataSource basicDataSource;
=======
    @Bean
    public BasicDataSource dataSource()
    {
        return basicDataSource;
    }
    @Bean
    public ShoppingCartDao shoppingCartDao()
    {
        // FIX: register ShoppingCartDao as a Spring bean so ShoppingCartController can be created
        return new MySqlShoppingCartDao(basicDataSource);
    }

    @Bean
    public CategoryDao categoryDao()
    {
        // FIX: register CategoryDao as a Spring bean so it can be injected
        // Used the existing MySQLCategoriesDao implementation (no new DAO created)
        return new MySqlCategoryDao(basicDataSource);
    }
>>>>>>> adaf4d6 (Made few changes)

    @Autowired
    public DatabaseConfig(@Value("${datasource.url}") String url,
                          @Value("${datasource.username}") String username,
                          @Value("${datasource.password}") String password) {

        basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
    }

    @Bean
    public BasicDataSource dataSource() {
        return basicDataSource;
    }


    @Bean
    public JdbcTemplate jdbcTemplate(BasicDataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public ShoppingCartDao shoppingCartDao(JdbcTemplate jdbcTemplate) {
        return new MySqlShoppingCartDao(jdbcTemplate);
    }
}

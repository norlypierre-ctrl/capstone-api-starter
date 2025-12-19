package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MYSqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {


    public MYSqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }


    @Override
    public ShoppingCart getByUserId(int userId) {
        String getByIdQuery = """
                SELECT user_id, S.product_id, quantity, products.*
                FROM shopping_cart S JOIN Products USING (product_id)
                WHERE user_id = ?
                """;

                ShoppingCart cart = new ShoppingCart();

                try (Connection connection = getConnection();
                     PreparedStatement statement = connection.prepareStatement(getByIdQuery)) {

                    statement.setInt(1, userId);

                    ResultSet row = statement.executeQuery();

                    while (row.next()) {

                        Product product = mapRow(row);

                        int quantity = row.getInt("quantity");

                        ShoppingCartItem cartItem = new ShoppingCartItem(product, quantity);
                        cart.add(cartItem);
                    }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                return cart;
            }

            @Override
            public void addItemToCart(int userId, int productID) {
                String addItemQuery = """
                INSERT INTO shopping_cart (user_id, product_id, quantity)
                VALUES (?, ?, 1) ON DUPLICATE KEY UPDATE quantity = quantity + 1
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(addItemQuery)) {

            statement.setInt(1, userId);
            statement.setInt(2, productID);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateQuantity(int userId, int productId, int quantity) {
        String updateQuery = """
                UPDATE shopping_cart SET quantity = ? WHERE user_id = ?
                AND product_id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {

                statement.setInt(1, quantity);
                statement.setInt(2, userId);
                statement.setInt(3, productId);

                statement.executeUpdate();

                if (quantity <= 0) {
                    String deleteQuery = """
                            DELETE FROM shopping_cart WHERE user_id = ?
                            AND product_id = ?""";

                    try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {

                        deleteStatement.setInt(1, userId);
                        deleteStatement.setInt(2, productId);

                        deleteStatement.executeUpdate();
                    }
                }
        } catch(SQLException e) {
            throw new RuntimeException(e);
    }
}

    @Override
    public void clearCart(int userId) {
        String addItemQuery = """
                DELETE FROM shopping_cart WHERE user_id = ?""";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(addItemQuery)) {

            statement.setInt(1, userId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Product mapRow (ResultSet row) throws SQLException
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

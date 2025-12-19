package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {

    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Order create(Order order, ShoppingCart cart) {
        String orderQuery = """
                INSERT INTO orders (user_id, address, city, state, zip, date, shipping_amount)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        String lineItemQuery = """
                INSERT INTO order_line_items (order_id, product_id, sales_price, quantity, discount)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement oStatement = connection.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement lStatement = connection.prepareStatement(lineItemQuery)) {

            oStatement.setInt(1, order.getUserId());
            oStatement.setString(2, order.getAddress());
            oStatement.setString(3, order.getCity());
            oStatement.setString(4, order.getState());
            oStatement.setString(5, order.getZip());
            oStatement.setTimestamp(6, Timestamp.valueOf(order.getDate()));
            oStatement.setBigDecimal(7, order.getShippingAmount());

            oStatement.executeUpdate();

            try (ResultSet generatedKeys = oStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setOrderId(generatedKeys.getInt(1));
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            for (ShoppingCartItem item : cart.getItems().values()) {
                lStatement.setInt(1, order.getOrderId());
                lStatement.setInt(2, item.getProductId());
                lStatement.setBigDecimal(3, item.getProduct().getPrice());
                lStatement.setInt(4, item.getQuantity());
                lStatement.setBigDecimal(5, item.getDiscountPercent());
                lStatement.addBatch();
            }

            lStatement.executeBatch();
            return order;

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}






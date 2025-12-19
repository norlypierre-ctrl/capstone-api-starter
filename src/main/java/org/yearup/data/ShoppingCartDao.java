package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    void addItemToCart(int userId, int id);
    void updateQuantity(int userId, int productId, int quantity);
    void clearCart(int userId);
}

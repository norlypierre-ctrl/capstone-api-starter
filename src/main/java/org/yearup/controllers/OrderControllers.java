package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;
import org.yearup.models.User;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/orders")
@CrossOrigin
public class OrderControllers {
    private OrderDao orderDao;
    private ShoppingCartDao shoppingCartDao;
    private ProfileDao profileDao;
    private UserDao userDao;

    @Autowired
    public OrderControllers(OrderDao orderDao, ShoppingCartDao shoppingCartDao,
                            ProfileDao profileDao, UserDao userDao) {
        this.orderDao = orderDao;
        this.shoppingCartDao = shoppingCartDao;
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public Order checkout(Principal principal) {

        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);

            int userId = user.getId();

            Profile profile = profileDao.getByUserId(userId);
            ShoppingCart shoppingCart = shoppingCartDao.getByUserId(userId);

            Order order = new Order();

            order.setUserId(userId);
            order.setAddress(profile.getAddress());
            order.setCity(profile.getCity());
            order.setState(profile.getState());
            order.setZip(profile.getZip());
            order.setDate(LocalDateTime.now());
            order.setShippingAmount(BigDecimal.ZERO);

            Order ordered = orderDao.create(order, shoppingCart);

            shoppingCartDao.clearCart(userId);

            return ordered;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}
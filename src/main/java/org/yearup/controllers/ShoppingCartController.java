package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@RequestMapping ("cart")
@CrossOrigin
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class ShoppingCartController
{
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    @GetMapping
    public ShoppingCart getCart(Principal principal)
    {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);

            int userId = user.getId();

            return shoppingCartDao.getByUserId(userId);
        }
        catch (Exception ex){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PostMapping("/products/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addItemToCart(Principal principal, @PathVariable int id){
        try {
            String username = principal.getName();
            User user = userDao.getByUserName(username);

            int userId = user.getId();
            shoppingCartDao.addItemToCart(userId, id);
        }
        catch (Exception ex){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PutMapping("/products/{id}")
    public void updateQuantity(Principal principal, @PathVariable int id, @RequestBody ShoppingCartItem item){
        try {
            String username = principal.getName();
            User user = userDao.getByUserName(username);

            int userId = user.getId();
            shoppingCartDao.updateQuantity(userId, id, item.getQuantity());
        }
        catch (Exception ex){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Principal principal) {
        try {
            String username = principal.getName();
            User user = userDao.getByUserName(username);

            int userId = user.getId();
            shoppingCartDao.clearCart(userId);
        }
        catch (Exception ex){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}
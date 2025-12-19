USE recordshop;

ALTER TABLE shopping_cart
ADD UNIQUE (user_id, product_id);
# Secure Shopping Cart Backend

Production-ready Spring Boot backend for shopping cart, checkout, and admin product/category/order/user management.

## Tech Stack
- Java 17
- Spring Boot 3
- Spring Web
- Spring Security
- JWT Authentication
- Spring Data JPA
- MySQL
- Maven
- Lombok
- Validation

## Run
1. Ensure MySQL is running.
2. Update credentials in `src/main/resources/application.properties` if required.
3. Optionally run `src/main/resources/mysql-schema.sql` manually.
4. Start app:

```bash
mvn spring-boot:run
```

## Default Seed Data
- Roles: `ROLE_ADMIN`, `ROLE_USER`
- Categories:
  - Electronics
  - Mobiles
  - Laptops
  - Toys
  - Clothing
  - Home Appliances
  - Books
  - Furniture
  - Sports
  - Groceries
- Admin user:
  - username: `admin`
  - password: `Admin@123`

## Security
- JWT expiration: 24 hours (`86400000 ms`)
- Expired token response:
  - `{"message":"Token expired. Please login again."}`
- Authorization:
  - `/api/auth/**` public
  - `/api/admin/**` requires `ROLE_ADMIN`
  - `/api/cart/**`, `/api/orders/**` requires `ROLE_USER`
  - product/category read APIs require authenticated user

## Main API Endpoints
- Auth
  - `POST /api/auth/register`
  - `POST /api/auth/login`
- Category
  - `GET /api/categories`
  - `POST /api/admin/categories`
  - `DELETE /api/admin/categories/{id}`
- Product
  - `GET /api/products`
  - `GET /api/products/{id}`
  - `GET /api/products/search?name=`
  - `GET /api/products/category/{categoryId}`
- Admin Product
  - `POST /api/admin/products`
  - `PUT /api/admin/products/{id}`
  - `DELETE /api/admin/products/{id}`
  - `PATCH /api/admin/products/{id}/stock`
  - `PATCH /api/admin/products/{id}/disable`
- Cart
  - `POST /api/cart/add/{productId}`
  - `DELETE /api/cart/remove/{productId}`
  - `PUT /api/cart/update/{productId}`
  - `GET /api/cart`
- Orders
  - `POST /api/orders/checkout`
  - `GET /api/orders/user`
  - `GET /api/admin/orders`
- Admin Users
  - `GET /api/admin/users`

## Postman Sample Request Bodies

### Register
```json
{
  "username": "johnuser",
  "email": "john@example.com",
  "password": "John@123"
}
```

### Login
```json
{
  "username": "johnuser",
  "password": "John@123"
}
```

### Add Product (Admin)
```json
{
  "name": "iPhone 14",
  "description": "Apple smartphone",
  "price": 80000,
  "stockQuantity": 10,
  "categoryId": 2
}
```

### Add to Cart
```json
{
  "quantity": 1
}
```

### Checkout
```json
{
  "paymentMethod": "CASH_ON_DELIVERY"
}
```

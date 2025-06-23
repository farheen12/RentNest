# 🏠 RentNest - Airbnb Clone Backend

A Spring Boot-based backend project that powers a rental booking platform, mimicking core Airbnb functionalities.
## 🔗 Live Swagger Docs (API Reference)

📄 [Swagger API Documentation](http://localhost:8080/api/v1/swagger-ui/index.html)

## 🔧 Tech Stack
- Java 17
- Spring Boot, Spring Security, Spring Data JPA
- JWT Authentication
- Stripe Payment Integration
- PostgreSQL
- Decorator Design Pattern for Dynamic Pricing

## ✅ Features
- User SignUp/Login with JWT-based Auth
- Role-based access control for Admins (Hotel Managers)
- Hotel and Room CRUD APIs
- Booking APIs with pessimistic locking to handle concurrency
- Stripe integration and webhook handling
- Admin APIs for Inventory Management
- Dynamic pricing using decorator pattern and scheduling through cron job

## 🔐 Security
- Passwords stored using BCrypt
- Stateless Auth with JWT
- Secure Booking & Payment APIs

## 📦 Setup & Run
```bash
git clone https://github.com/farheen12/RentNest.git
cd RentNest
./mvnw spring-boot:run

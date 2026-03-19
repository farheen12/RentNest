# 🏠 RentNest - Airbnb Clone Backend

RentNest is a Spring Boot-based backend system for a rental booking platform, supporting secure bookings, dynamic pricing, and payment processing. The system is designed with scalability and real-world backend challenges such as concurrency handling, asynchronous workflows, and performance optimization.

---

## 🔧 Tech Stack

- **Backend:** Java 17, Spring Boot, Spring MVC  
- **Security:** Spring Security, JWT Authentication  
- **Database:** PostgreSQL, Redis  
- **Payments:** Stripe API (Webhook-based processing)  
- **Architecture:** REST APIs, Layered Architecture  
- **Tools:** Maven, Git  

---

## 🚀 System Highlights

- Implemented **dynamic pricing engine using Decorator Design Pattern** for flexible pricing rules  
- Designed a **daily scheduled job** to precompute and update pricing, reducing runtime computation  
- Applied **pessimistic locking** to prevent race conditions during concurrent bookings  
- Integrated **Stripe payments with webhook-based asynchronous confirmation**  
- Secured APIs using **JWT-based authentication and role-based authorization**  
- Used **Redis caching** to optimize search performance and reduce database load  

---

## 🏗 Architecture Overview

The application follows a **layered architecture**:

- **Controller Layer:** Handles incoming HTTP requests  
- **Service Layer:** Contains core business logic  
- **Repository Layer:** Manages database operations using JPA  

### Key Workflows:

- **Booking Flow:**  
  User → Booking API → Availability Check → Lock Resource (Pessimistic Locking) → Payment → Confirmation  

- **Pricing Flow:**  
  Base Price → Decorator Pattern Enhancements → Daily Scheduled Updates  

- **Payment Flow:**  
  Payment Request → Stripe API → Webhook Callback → Payment Status Update  

---

## ✅ Features

- User Signup/Login with JWT-based authentication  
- Role-based access control (Admin / Manager / User)  
- Hotel and Room management APIs  
- Booking system with concurrency handling  
- Payment processing with Stripe integration  
- Dynamic pricing with automated updates  
- Redis caching for optimized performance  

---

## 📡 Sample APIs

- **POST** `/auth/signup`  
- **POST** `/auth/login`  
- **GET** `/hotels`  
- **POST** `/bookings`  

👉 Swagger UI available at:  
`http://localhost:8080/api/v1/swagger-ui/index.html`

---

## 🔐 Security

- Passwords encrypted using BCrypt  
- Stateless authentication using JWT  
- Protected endpoints with role-based access control  

---

## ⚙️ Setup & Run

```bash
git clone https://github.com/farheen12/RentNest.git
cd RentNest
./mvnw spring-boot:run

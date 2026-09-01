# 🚗 RidePulse — Real-Time Ride-Booking Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3+-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%20%2F%2021-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg?logo=mysql)](https://www.mysql.com/)
[![React](https://img.shields.io/badge/React-18.x-61DAFB.svg?logo=react)](https://react.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3.x-38B2AC.svg?logo=tailwind-css)](https://tailwindcss.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**RidePulse** is a production-grade, full-stack ride-hailing application (Mini-Uber / Ola) engineered to demonstrate core backend competencies: **ACID transactional integrity**, **concurrency control (pessimistic locking)**, **geospatial proximity querying (Haversine formula)**, and **real-time bi-directional messaging via STOMP WebSockets**.

---

## 📸 Key Features

### 👤 Rider Experience
* **Interactive Map & Geolocation:** Free OpenStreetMap + Leaflet.js integration for pin-point pickup and drop-off selection.
* **Instant Dynamic Fare Estimate:** Real-time pricing calculation across vehicle tiers (`BIKE`, `HATCHBACK`, `SEDAN`, `SUV`).
* **Ride Booking & OTP Generation:** Automatic generation of a secure 4-digit start OTP.
* **Live Driver Tracking:** Real-time vehicle location updates streamed over WebSockets directly to the map.
* **Mock Payments & Feedback:** Seamless payment settlement (Cash / UPI / Card) and 1–5 star driver reviews.

### 🚘 Driver Cockpit
* **Online/Offline Availability Switch:** Real-time status toggle with live GPS heartbeat pings.
* **Instant Ride Dispatch Alerts:** Incoming ride requests within a 5 km radius received via dedicated WebSocket channels.
* **Concurrency-Safe Ride Acceptance:** Guaranteed single-driver assignment under high concurrent requests.
* **Secure Trip Lifecycle:** OTP validation to initiate trips and automated fare calculation on completion.

### 🛡️ Core Engineering & Security
* **Role-Based Access Control (RBAC):** Stateless Spring Security 6 with HMAC-SHA256 JWT authentication (`ROLE_RIDER`, `ROLE_DRIVER`, `ROLE_ADMIN`).
* **Zero Race Conditions:** Pessimistic row locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) prevents multiple drivers from accepting the same ride simultaneously.
* **Interactive API Documentation:** Interactive Swagger UI documentation at `/swagger-ui.html`.

---

## 🏛️ System Architecture

```
+-----------------------------------------------------------------------------------+
|                                 REACT 18+ CLIENT                                  |
|   [Rider Experience]         [Driver Cockpit]            [Admin Live Overview]    |
|  - OpenStreetMap (Leaflet)   - Availability Toggle (On)  - Active Trip Monitor     |
|  - Live Fare Calculation     - Instant Dispatch Alerts   - Driver Roster & Stats  |
|  - Real-time Driver Tracker  - OTP Verification Card     - Revenue Breakdown      |
+-----------------------------------------------------------------------------------+
                                   │                 ▲
                     REST (JSON)   │                 │  STOMP over WebSocket
                      (Axios)      ▼                 │  (SockJS / StompJS)
+-----------------------------------------------------------------------------------+
|                             SPRING BOOT 3.3+ BACKEND                              |
|                                                                                   |
|  [Security & Auth]        [WebSocket Broker]        [Global Exception & Advice]   |
|  - JWT Filter (HMAC-256)  - /topic (Broadcast)      - ApiResponse<T> Wrapper      |
|  - Stateless Session      - /user/queue (Targeted)  - Domain Custom Exceptions    |
|  - Role-Based Auth (RBAC) - Channel Interceptors    - Jakarta Bean Validation     |
|                                                                                   |
|  [Service Layer (Clean Interface & Implementations)]                              |
|  - AuthService            - RideMatchingService     - RideLifecycleService        |
|  - FareCalculationService - DriverTrackingService   - PaymentService              |
|                                                                                   |
|  [Concurrency & Data Access Layer (Spring Data JPA / Hibernate)]                  |
|  - Pessimistic Row Locking (`@Lock(PESSIMISTIC_WRITE)`) on Ride Acceptance        |
|  - Native MySQL Haversine Spatial Query for Driver Proximity Discovery            |
+-----------------------------------------------------------------------------------+
                                         │
                                         ▼
+-----------------------------------------------------------------------------------+
|                             MYSQL 8.0+ (InnoDB ENGINE)                            |
|  - users                  - driver_profiles         - rides                       |
|  - payments               - ratings                 - Optimized Indexes           |
+-----------------------------------------------------------------------------------+
```

---

## 🔄 Ride Lifecycle State Machine

```
   ┌─────────────┐      Driver Accepts      ┌────────────┐
   │  REQUESTED  │ ───────────────────────► │  ACCEPTED  │
   └─────────────┘ (Pessimistic Lock Row)   └────────────┘
          │                                        │
          │ Rider Cancels                          │ Driver Arrived at Pickup
          ▼                                        ▼
   ┌─────────────┐                          ┌────────────┐
   │  CANCELLED  │ ◄─────────────────────── │  ARRIVED   │
   └─────────────┘     Driver/Rider Cancel  └────────────┘
                                                   │
                                                   │ Driver Validates 4-Digit OTP
                                                   ▼
                                            ┌─────────────┐
                                            │ IN_PROGRESS │
                                            └─────────────┘
                                                   │
                                                   │ Driver Completes Trip
                                                   ▼
                                            ┌─────────────┐
                                            │  COMPLETED  │
                                            └─────────────┘
                                                   │
                                                   ▼
                                            [Payment & Rating]
```

---

## 🛠️ Technology Stack

| Domain | Technology / Library | Purpose |
|---|---|---|
| **Backend** | Spring Boot 3.3+ (Java 17/21) | RESTful API, core business & transaction logic |
| **Database** | MySQL 8.0+ (InnoDB) | Relational data persistence, Spatial indexing |
| **Security** | Spring Security 6 + JWT | Stateless authentication & RBAC |
| **Real-Time** | Spring WebSocket + STOMP | Real-time driver dispatch & live GPS streaming |
| **Frontend** | React 18 (Vite) + Tailwind CSS | Fast, responsive Single Page Application (SPA) |
| **Maps & Geo** | Leaflet.js & OpenStreetMap | Zero-cost map tiles, custom car markers & routing |
| **Docs & Validation** | Springdoc OpenAPI (Swagger 3) | Interactive API exploration and testing |
| **Testing** | JUnit 5 + Mockito + AssertJ | Unit, integration, and multi-threaded concurrency tests |

---

## 🗄️ Database Schema Summary

* **`users`**: Base credentials, roles (`ROLE_RIDER`, `ROLE_DRIVER`, `ROLE_ADMIN`), contact information.
* **`driver_profiles`**: Vehicle specifications, availability flags, active latitude/longitude coordinates, ratings.
* **`rides`**: Full booking lifecycle data, pickup/drop coordinates, dynamic fare amount, 4-digit start OTP, and status.
* **`payments`**: Transaction records linked 1-to-1 with completed rides.
* **`ratings`**: Star ratings (1–5) and review feedback linked to trips.

---

## 🚀 Getting Started

### 📋 Prerequisites
Ensure you have the following installed locally:
* **JDK 17** or **JDK 21**
* **Maven 3.8+**
* **MySQL Server 8.0+**
* **Node.js 18+** & **npm**

---

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/ridepulse.git
cd ridepulse
```

---

### 2. Database Setup (MySQL)
Create a new MySQL database:
```sql
CREATE DATABASE ridepulse_db;
```

---

### 3. Backend Setup & Configuration
Navigate to the backend directory:
```bash
cd backend
```

Update your `src/main/resources/application.yml` (or `application.properties`) with your MySQL credentials:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ridepulse_db?useSSL=false&serverTimezone=UTC
    username: YOUR_MYSQL_USERNAME
    password: YOUR_MYSQL_PASSWORD
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect

jwt:
  secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  expiration-ms: 900000 # 15 minutes
  refresh-expiration-ms: 604800000 # 7 days
```

Build and run the backend server:
```bash
mvn clean install
mvn spring-boot:run
```
The backend will start at: `http://localhost:8080`  
Swagger UI is accessible at: `http://localhost:8080/swagger-ui.html`

---

### 4. Frontend Setup & Configuration
Open a new terminal window:
```bash
cd frontend
npm install
npm run dev
```
The client will start at: `http://localhost:5173`

---

## 🧪 Running Tests & Concurrency Verification
Run the backend test suite, including multi-threaded race condition tests:
```bash
cd backend
mvn test
```

---

## 📌 Core REST API Matrix

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register a new Rider or Driver account |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user & receive JWT tokens |
| `GET` | `/api/v1/auth/me` | Authenticated | Retrieve profile details of the active session |
| `POST` | `/api/v1/rides/estimate` | Rider | Calculate distance, duration, and fare by vehicle tier |
| `POST` | `/api/v1/rides/request` | Rider | Create ride request, generate OTP, alert nearby drivers |
| `POST` | `/api/v1/rides/{id}/accept` | Driver | Concurrency-safe ride acceptance (`@Lock`) |
| `POST` | `/api/v1/rides/{id}/arrived` | Driver | Update ride state to `ARRIVED` |
| `POST` | `/api/v1/rides/{id}/start` | Driver | Validate 4-digit OTP & transition state to `IN_PROGRESS` |
| `POST` | `/api/v1/rides/{id}/complete` | Driver | End trip and generate pending payment invoice |
| `POST` | `/api/v1/rides/{id}/cancel` | Rider/Driver | Cancel ride with validation |
| `POST` | `/api/v1/payments/{rideId}/pay` | Rider | Process payment (Cash, UPI, Mock Card) |
| `POST` | `/api/v1/ratings/{rideId}` | Rider | Submit 1–5 star driver rating & feedback |

---

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).

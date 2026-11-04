# StayNest — Airbnb-Inspired Vacation Rental Booking Platform

**StayNest** is a modern, responsive, full-stack vacation rental booking platform inspired by Airbnb. Built with Java, Spring Boot, MySQL, Spring Security (JWT), HTML5, CSS3, JavaScript, and Bootstrap 5, it delivers an intuitive and visually stunning travel-booking experience for discovering and reserving beachfront villas, nordic timber cabins, historic palazzos, and luxury mountain chalets.

---

## 🌟 Key Highlights & Features

- **Airbnb-Inspired Design**: Elegant coral-red accents (`#FF385C`), floating search pills, rounded cards, interactive image mosaics, and micro-animations.
- **Role-Based Access Control**:
  - `ROLE_GUEST`: Discover stays, filter by amenities/price/rating, add favorites to Wishlist, calculate dynamic pricing, book demo reservations, cancel bookings, and review completed stays.
  - `ROLE_HOST`: Dedicated Host Operations Portal to publish listings, manage amenities and photo galleries, track gross revenue and occupancy metrics, and manage reservations.
  - `ROLE_ADMIN`: Platform Administration Console to oversee all users, activate/suspend accounts, adjust permissions, feature listings, and moderate reviews.
- **Concurrency & Overlap Prevention**: Robust server-side date validation prevents overlapping reservations using interval intersection logic:
  $$\text{Overlap} \iff (\text{existing.checkIn} < \text{requested.checkOut}) \land (\text{existing.checkOut} > \text{requested.checkIn})$$
- **Dynamic Real-Time Pricing Widget**: Automatically calculates base nights rate, cleaning fee, standard StayNest service fee (12%), and occupancy taxes (8%).
- **Interactive Wishlist**: 1-click favorite toggle with optimistic UI updates and instant persistence.
- **Curated Pre-Seeded Global Stays**: Includes verified stays across Malibu, Aspen, Paris, Venice, Zermatt, Tuscany, Lake Tahoe, and Kyoto.

---

## 🏗️ Technology Stack

| Layer | Technologies |
|---|---|
| **Frontend** | HTML5, CSS3, Vanilla JavaScript (ES6+ Modules), Bootstrap 5.3, Bootstrap Icons |
| **Backend** | Java 17+, Spring Boot 3.3.x, Spring Data JPA, Hibernate |
| **Security** | Spring Security 6, JWT (JSON Web Tokens), BCrypt Password Hashing, CORS |
| **Database** | MySQL 8.0+ (with optional H2 in-memory profile for zero-install demo runs) |
| **Build Tool** | Apache Maven |
| **Architecture** | Layered REST Architecture (Controller &rarr; Service &rarr; Repository &rarr; Entity / DTO) |

---

## 📂 Project Directory Structure

```text
d:\StayNest\
├── pom.xml                                # Maven build definition
├── README.md                              # Complete platform documentation
├── run.bat                                # Windows helper launcher
├── database/
│   ├── schema.sql                         # MySQL database schema & table constraints
│   └── data.sql                           # Initial seed data for users, stays, categories & amenities
├── frontend/                              # Standalone frontend distribution
│   ├── index.html                         # Home page with hero search & featured stays
│   ├── explore.html                       # Search & multi-filter listings catalog
│   ├── property-details.html              # Stay details, photo mosaic, and booking widget
│   ├── bookings.html                      # Guest reservation history & printable receipts
│   ├── wishlist.html                      # Saved favorite properties
│   ├── host-dashboard.html                # Host listing & reservation management
│   ├── admin-dashboard.html               # Administrator moderation console
│   ├── css/
│   │   ├── style.css                      # Coral-red design system & custom components
│   │   └── responsive.css                 # Tablet & mobile responsive rules
│   └── js/
│       ├── api.js                         # API client, JWT session handling, toasts & formatters
│       ├── main.js                        # Navigation bar, auth modal & demo login buttons
│       ├── home.js                        # Home page category pills & featured properties
│       ├── explore.js                     # Search, filter pills, sorting & dynamic grid
│       ├── property.js                    # Details page, image gallery, dynamic pricing & checkout
│       ├── bookings.js                    # Reservation lifecycle & cancellation
│       ├── wishlist.js                    # Wishlist management
│       ├── host.js                        # Host CRUD listings & reservation actions
│       └── admin.js                       # Admin platform statistics & user moderation
└── src/
    ├── main/
    │   ├── java/com/staynest/
    │   │   ├── StayNestApplication.java   # Spring Boot entry point
    │   │   ├── config/                    # Security, JWT Provider, Filters, CORS
    │   │   ├── controller/                # REST Controllers (Auth, Property, Booking, Host, Admin...)
    │   │   ├── dto/                       # Request/Response Data Transfer Objects
    │   │   ├── entity/                    # JPA Entities (User, Property, Booking, Review, Favorite...)
    │   │   ├── exception/                 # Global exception handler & custom exceptions
    │   │   ├── repository/                # Spring Data JPA Repositories
    │   │   └── service/                   # Business logic & DataInitializer (auto-seeding)
    │   └── resources/
    │       ├── application.properties     # MySQL configuration
    │       ├── application-h2.properties  # Standalone H2 zero-install profile
    │       └── static/                    # Frontend assets bundled for single-JAR execution
    └── test/
        └── java/com/staynest/             # Unit and integration test suite
```

---

## 👥 Pre-Configured Demo Accounts

For instant evaluation, the platform comes pre-seeded with three demo accounts:

| Role | Email | Password | Permissions |
|---|---|---|---|
| **Demo Guest** | `guest@staynest.com` | `guest123` | Book stays, manage trips, save wishlists, submit reviews |
| **Demo Host** | `host@staynest.com` | `host123` | Add/edit properties, manage amenities, oversee reservations |
| **Demo Admin** | `admin@staynest.com` | `admin123` | Moderate users, approve/hide listings, inspect platform revenue |

> **Pro Tip:** In the login modal, click any of the **"Demo Guest"**, **"Demo Host"**, or **"Demo Admin"** quick-fill buttons for immediate 1-click credential filling!

---

## 🚀 Setup & Execution Guide

### Option A: Running with MySQL (Production Mode)

1. **Start MySQL Server**:
   Ensure MySQL is running on `localhost:3306` (via MySQL Server, XAMPP, or Docker).
2. **Create the Database (Optional)**:
   Spring Boot will automatically create the database if it doesn't exist via `createDatabaseIfNotExist=true`. Alternatively, run `database/schema.sql`.
3. **Configure Database Credentials**:
   If your MySQL password is not blank, edit `src/main/resources/application.properties` or set environment variables:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/staynest_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```
4. **Run the Application**:
   - In IntelliJ IDEA or VS Code: Open the `StayNest` folder, navigate to `com.staynest.StayNestApplication.java`, and click **Run**.
   - Or via command line:
     ```bash
     mvn spring-boot:run
     ```
5. **Open the Web Application**:
   Navigate to [http://localhost:8080](http://localhost:8080) in your web browser.

---

### Option B: Zero-Install Standalone Mode (H2 In-Memory)

If you don't have MySQL installed locally and want to test the full platform immediately:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
Or set `-Dspring.profiles.active=h2` in your IDE run configuration.
All data, demo users, categories, amenities, and properties will be automatically seeded into an in-memory database with full MySQL compatibility!
- Web Application: [http://localhost:8080](http://localhost:8080)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:staynest_db`, User: `sa`, Password: blank)

---

## 📡 REST API Documentation

### 1. Authentication (`/api/auth`)
- `POST /api/auth/register` — Register a new account (`RegisterRequest`).
- `POST /api/auth/login` — Sign in and receive a JWT Bearer token (`AuthRequest`).
- `GET /api/auth/me` — Retrieve the currently authenticated user's profile.
- `PUT /api/auth/profile` — Update name, phone, bio, or avatar.
- `POST /api/auth/switch-role?role=ROLE_HOST` — Switch between Guest and Host mode.

### 2. Properties & Search (`/api/properties`)
- `GET /api/properties` — Search stays with multi-criteria filtering:
  - `query` (Destination city, state, country, or title)
  - `category` (Slug: beachfront, cabins, villas, etc.)
  - `propertyType` (Villa, Cabin, Apartment, Chalet, etc.)
  - `minPrice` & `maxPrice`
  - `guests` (Minimum capacity)
  - `minRating` (4.5+, 4.8+)
  - `checkIn` & `checkOut` (Excludes reserved dates)
  - `amenityIds` (List of amenity IDs)
  - `sortBy` (`price_asc`, `price_desc`, `rating`, `popularity`, `newest`)
- `GET /api/properties/featured` — Retrieve featured high-rated stays.
- `GET /api/properties/{id}` — Full stay details including images, host, and amenities.
- `GET /api/properties/{id}/similar` — 4 similar stay recommendations.
- `GET /api/properties/{id}/booked-dates` — Array of reserved date ranges for calendar blocking.

### 3. Bookings (`/api/bookings`)
- `POST /api/bookings` — Create a reservation with date interval conflict check.
- `GET /api/bookings/my` — Retrieve the authenticated guest's bookings.
- `GET /api/bookings/code/{code}` — Fetch reservation by unique code (e.g. `SN-2026-9812`).
- `PUT /api/bookings/{id}/cancel` — Cancel a reservation and trigger simulated refund.
- `GET /api/bookings/calculate-price` — Query real-time calculation breakdown for check-in/out.

### 4. Wishlists (`/api/favorites`)
- `POST /api/favorites/{propertyId}/toggle` — Toggle favorite state (add/remove).
- `GET /api/favorites` — Retrieve all favorited properties for the logged-in user.
- `GET /api/favorites/{propertyId}/check` — Check if a stay is favorited.

### 5. Reviews (`/api/reviews`)
- `POST /api/reviews` — Submit 1-5 star review and comment (auto-recalculates property rating).
- `GET /api/reviews/property/{propertyId}` — Retrieve reviews for a stay.
- `DELETE /api/reviews/{id}` — Delete review (author or admin only).

### 6. Host Operations (`/api/host`)
- `GET /api/host/stats` — Host metrics (total listings, reservations, gross earnings, average rating).
- `GET /api/host/properties` — Retrieve listings created by the host.
- `POST /api/host/properties` — Publish a new vacation stay.
- `PUT /api/host/properties/{id}` — Edit listing details, pricing, and images.
- `DELETE /api/host/properties/{id}` — Deactivate a listing.
- `GET /api/host/reservations` — Retrieve all reservations booked on host's stays.
- `PUT /api/host/reservations/{id}/status?status=CANCELLED` — Update reservation status.

### 7. Administrator Console (`/api/admin`)
- `GET /api/admin/stats` — Platform metrics (users, listings, bookings, platform revenue).
- `GET /api/admin/users` — List all registered users.
- `PUT /api/admin/users/{id}/status?active=false` — Activate or suspend a user account.
- `PUT /api/admin/users/{id}/role?role=ROLE_HOST` — Change a user's role.
- `GET /api/admin/properties` — List all properties across the platform.
- `PUT /api/admin/properties/{id}/toggle-active` — Moderate property visibility.
- `PUT /api/admin/properties/{id}/toggle-featured` — Toggle featured badge status.
- `GET /api/admin/reviews` — List all community reviews.
- `PUT /api/admin/reviews/{id}/toggle-moderated` — Flag and hide spam or inappropriate reviews.

---

## 🛡️ Security Implementation

- **Stateless JWT**: Secure token generation via HMAC-SHA256 (`io.jsonwebtoken`).
- **Password Protection**: Passwords securely hashed with BCrypt.
- **Authentication Filter**: `JwtAuthenticationFilter` intercepts HTTP requests, extracts Bearer tokens, and populates `SecurityContextHolder`.
- **Role-Based Authorization**: Methods and routes protected via `@PreAuthorize` and Spring Security URL patterns.
- **Input Validation**: Server-side Bean Validation (`@NotNull`, `@Size`, `@DecimalMin`, `@FutureOrPresent`) rejects malformed payloads.

---

## 🧪 Testing Checklist

- [x] **Home Page**: Brand logo, interactive hero search bar, category navigation bar, featured stays grid.
- [x] **Search & Explore**: Destination, date ranges, guest count, price sliders, amenities checkboxes, sorting.
- [x] **Property Details**: High-resolution image mosaic, host info, amenities with icons, reviews list.
- [x] **Dynamic Pricing**: Instant client-side breakdown (nights x price, cleaning fee, 12% service fee, 8% taxes).
- [x] **Booking Concurrency**: Verified that overlapping reservations for the same stay return an error and cannot be booked.
- [x] **Demo Checkout**: Modal with demo payment selection, generating unique code (`SN-2026-XXXX`).
- [x] **Guest Dashboard**: Upcoming trips, past stays, receipt printer, and cancellation with refund status.
- [x] **Wishlists**: 1-click heart toggle with instant badge animation and persistence.
- [x] **Host Dashboard**: Total listings, reservations, earnings, property creator, reservation status updater.
- [x] **Admin Console**: Platform analytics, user status toggling, property moderation, and review control.

---

&copy; 2026 StayNest. Designed and crafted as a production-grade full-stack portfolio platform.

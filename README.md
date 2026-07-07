# Halo Aligners Backend

A Spring Boot and Kotlin-based backend application for managing Halo Aligners operations, featuring JWT-based authentication, user role management, and secure endpoints.

## 🔑 Available User Roles & Permissions

Authentication and Authorization in the system are handled using Spring Security with stateless JWT tokens (see [SecurityConfig](src/main/kotlin/com/haloalligners/security/SecirutyConfig.kt)).

The following roles are currently defined and available within the system:

| Role Name | Scope & Purpose | Default / Seeded Setup |
| :--- | :--- | :--- |
| **`SUPER_ADMIN`** | High-level administrative access to oversee the entire platform, manage users, and perform admin-specific workflows. | Automatically seeded on application startup if not already present by [DataLoader](src/main/kotlin/com/haloalligners/config/DataLoader.kt).<br>• Username: `superadmin`<br>• Default Password: `admin123` |
| **`DOCTOR`** | Standard role for dental professionals / clinic partners who submit cases, order aligners, and manage patient treatments. | Assigned by passing `"userRole": "DOCTOR"` during registration. (See [new.sh](new.sh)). |
| **`USER`** | Default role for standard registered entities or patients, if no other role is explicitly specified during registration. | Default fallback value defined in [UserEntity](src/main/kotlin/com/haloalligners/model/UserEntity.kt) (`role: String = "USER"`). |

---

## 🔒 Security & Authorization Model

1. **Token Authentication:** Stateless JWT authentication is enforced on all endpoints via [JwtAuthenticationFilter](src/main/kotlin/com/haloalligners/security/JwtAuthenticationFilter.kt).
2. **Access Control:**
   - **Public Endpoints:** `/api/auth/register` and `/api/auth/login` are publicly accessible.
   - **Authenticated Endpoints:** All other endpoints (e.g. `/api/secure/hello`) require a valid JWT token.
3. **Role Propagation:** User roles are loaded from the database and wrapped as Spring Security GrantedAuthorities via [CustomUserDetailsService](src/main/kotlin/com/haloalligners/security/CustomUserDetailsService.kt).

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK):** Version 17 or higher
- **Database:** PostgreSQL instance running locally or externally

### Configuration
Update the [application.yaml](src/main/resources/application.yaml) file or set the following environment variables:
- `DATASOURCE_URL`: Database connection string (default: `jdbc:postgresql://localhost:5432/haloaligners_db`)
- `DATASOURCE_USERNAME`: Database username (default: `omm`)
- `DATASOURCE_PASSWORD`: Database password (default: `root`)

### Running the Application
Use the Maven wrapper to start the Spring Boot application:
```bash
./mvnw spring-boot:run
```

---

## 📬 API Endpoints

### 1. Authentication

#### Register User / Doctor
* **URL:** `/api/auth/register`
* **Method:** `POST`
* **Headers:** `Content-Type: application/json`
* **Payload:**
```json
{
    "username": "drsmith",
    "password": "password123",
    "userRole": "DOCTOR",
    "fullName": "Dr. John Smith",
    "email": "john.smith@example.com",
    "phone": "1234567890",
    "gstNumber": "GSTIN12345",
    "clinicName": "Smith's Dental Clinic"
}
```

#### User Login
* **URL:** `/api/auth/login`
* **Method:** `POST`
* **Headers:** `Content-Type: application/json`
* **Payload:**
```json
{
    "username": "drsmith",
    "password": "password123"
}
```
* **Response:**
```json
{
    "status": 200,
    "message": "User logged in successfully",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9..."
    }
}
```

### 2. Secure Endpoints

#### Secure Hello
* **URL:** `/api/secure/hello`
* **Method:** `GET`
* **Headers:** `Authorization: Bearer <JWT_TOKEN>`

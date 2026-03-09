# Escuela Colombiana de Ingeniería Julio Garavito
## Software Architecture – ARSW
### Lab – Part 2: BluePrints API with JWT Security (OAuth 2.0)

This lab extends **Part 1** ([Lab_P1_BluePrints_Java21_API](https://github.com/TerraFour-ECI/arsw-blueprints-api-lab)) by adding **API security** using **Spring Boot 3, Java 21, and JWT (OAuth 2.0)**.  
The API becomes a **Resource Server** protected by Bearer tokens signed with **RS256**.  
It includes a didactic `/auth/login` endpoint that issues the token to facilitate testing.

---

## Objectives

- Implement security in REST services using **OAuth2 Resource Server**.
- Configure **JWT** issuance and validation.
- Protect endpoints with **roles and scopes** (`blueprints.read`, `blueprints.write`).
- Integrate security documentation into **Swagger/OpenAPI**.

---

## Requirements

- JDK 21
- Maven 3.9+
- Git

---

## Running the project

1. Clone or unzip the project:
   ```bash
   git clone https://github.com/DECSIS-ECI/Lab_P2_BluePrints_Java21_API_Security_JWT.git
   cd Lab_P2_BluePrints_Java21_API_Security_JWT
   ```
   Or if the professor provides a `.zip`, unzip it and navigate into the folder.

2. Run with Maven:
   ```bash
   mvn -q -DskipTests spring-boot:run
   ```

3. Verify the application starts at `http://localhost:8080`.

---

## Main Endpoints

### 1. Login (issues token)
```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "student",
  "password": "student123"
}
```
Response:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### 2. Get blueprints (requires scope `blueprints.read`)
```
GET http://localhost:8080/api/blueprints
Authorization: Bearer <ACCESS_TOKEN>
```

### 3. Create blueprint (requires scope `blueprints.write`)
```
POST http://localhost:8080/api/blueprints
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json

{
  "name": "New Blueprint"
}
```

---

## Swagger UI

- URL: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Click **Authorize**, enter the token in the following format:
  ```
  Bearer eyJhbGciOi...
  ```

---

## Project Structure

```
src/main/java/co/edu/eci/blueprints/
  ├── api/BlueprintController.java       # Protected endpoints
  ├── auth/AuthController.java           # Didactic login to issue tokens
  ├── config/OpenApiConfig.java          # Swagger + JWT configuration
  └── security/
       ├── SecurityConfig.java
       ├── MethodSecurityConfig.java
       ├── JwtKeyProvider.java
       ├── InMemoryUserService.java
       └── RsaKeyProperties.java
src/main/resources/
  └── application.yml
```

---

## Proposed Activities

1. Review the security configuration code (`SecurityConfig`) and identify how public and protected endpoints are defined.
2. Explore the login flow and analyze the claims in the issued JWT.
3. Extend the scopes (`blueprints.read`, `blueprints.write`) to control other API endpoints from the P1 lab.
4. Modify the token expiration time and observe the effect.
5. Document the authentication and business endpoints in Swagger.

---

## Recommended Reading

- [Spring Security Reference – OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Spring Boot – Securing Web Applications](https://spring.io/guides/gs/securing-web/)
- [JSON Web Tokens – jwt.io](https://jwt.io/introduction)

---

## License

Educational project for academic purposes – Escuela Colombiana de Ingeniería Julio Garavito.

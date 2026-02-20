# SmartProcure – Multi-Vendor Procurement System

SmartProcure is a B2B procurement platform backend built with **Java 17**, **Spring Boot**, **MySQL**, **Spring Security**, **JWT**, **Swagger/OpenAPI**, and **JPA/Hibernate**.

This codebase is structured as a **modular monolith** and follows a strict layered architecture:

- `controller` – HTTP/API layer (no business logic)
- `service` – business logic and workflows
  - `service.impl` – service implementations
- `repository` – database access
- `entity` – JPA entities and enums
- `dto` – request/response DTOs (no entity exposure)
- `config` – cross-cutting configuration (Swagger, CORS, etc.)
- `security` – Spring Security, JWT filters, handlers
- `exception` – custom exceptions and global exception handler
- `util` – helpers, mappers, API response wrappers

## Running locally

1. Create a MySQL database (e.g. `smartprocure_db`) and a user with appropriate privileges.
2. Update `spring.datasource.username` and `spring.datasource.password` in `application.yml`.
3. Ensure Java 17 and Maven are installed.
4. From the project root, run:

```bash
mvn spring-boot:run
```

Then open `http://localhost:8080/api/health` to verify the service is running.

Swagger UI will be available at `http://localhost:8080/swagger-ui.html` once the security module is configured.

## Day 2 – Database & entities

- **Schema**: Tables `roles`, `users`, `vendors`, `procurement_requests`, `quotations`, `orders`, `payments` with FKs, unique constraints, and indexes.
- **Entities**: JPA entities with `BaseEntity` (id, createdAt, updatedAt), enums (`RequestStatus`, `QuotationStatus`, `OrderStatus`, `PaymentStatus`), LAZY relationships, and `@Enumerated(STRING)`.
- **Repositories**: `RoleRepository`, `UserRepository`, `VendorRepository`, `ProcurementRequestRepository`, `QuotationRepository`, `OrderRepository`, `PaymentRepository` with the required finder methods.
- **Seed data**: `SeedDataRunner` inserts 3 roles, 1 admin, 1 customer, 1 vendor user, and 1 vendor profile when the DB is empty (password: `password`).
- **SQL validation**: Example JOIN queries are in `src/main/resources/schema-validation-queries.sql` for verifying quotations by request, order with vendor/customer, and payments by order.


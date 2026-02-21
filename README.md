🚀 SmartProcure – Full Stack B2B Procurement System

SmartProcure is a full-stack B2B procurement management platform designed for enterprise workflows.

It enables:

Customers to create procurement requests

Vendors to submit quotations

Admins to manage users and vendors

The system is built with a secure JWT-based authentication system and follows a clean layered backend architecture.

🏗 Architecture Overview

SmartProcure follows a Modular Monolith architecture with strict separation of concerns:

controller → service → repository → entity
Backend Structure

controller – REST API layer (no business logic)

service – business rules & workflows

repository – JPA data access

entity – database models

dto – request/response models (no entity exposure)

security – JWT, filters, Spring Security config

exception – centralized error handling

config – Swagger, CORS, and application configuration

🛠 Tech Stack
Backend

Java 17

Spring Boot

Spring Security

JWT Authentication

Spring Data JPA (Hibernate)

MySQL

Swagger / OpenAPI

SLF4J / Logback

Frontend

React (Vite)

Axios

Role-based UI rendering

Global JWT interceptor

CoreUI-based dashboard layout (customized)

📂 Project Structure
SmartProcure/
│
├── src/               # Spring Boot application
│
├── SmartProcure-Frontend/ # React frontend (Port 3000)
│
└── README.md
🗄 Database

Database name:

smartprocure_db

Core tables:

roles

users

vendors

procurement_requests

quotations

orders

payments

Relationships are enforced via foreign keys and indexed where necessary.

🔐 Security

Stateless JWT Authentication

Role-Based Access Control (ADMIN / CUSTOMER / VENDOR)

Global 401 interceptor on frontend

Password hashing using BCrypt

No entity exposure (DTO-based API responses)

🚀 Running the Project Locally
1️⃣ Database Setup

Create database:

CREATE DATABASE smartprocure_db;

Update credentials in:

backend/src/main/resources/application.yml

Example:

spring:
datasource:
url: jdbc:mysql://localhost:3306/smartprocure_db
username: root
password: yourpassword
2️⃣ Run Backend (Port 8081)

From backend folder:

mvn spring-boot:run

Backend will start at:

http://localhost:8081

Health Check:

http://localhost:8081/api/health

Swagger UI:

http://localhost:8081/swagger-ui.html
3️⃣ Run Frontend (Port 3000)

From frontend folder:

npm install
npm start

Frontend will run at:

http://localhost:3000
🔄 Authentication Flow

User logs in via /api/auth/login

Backend validates credentials

JWT token is returned

Frontend stores token in localStorage

Axios attaches token to every request

Global interceptor handles 401 (auto logout)

📊 Implemented Features
Backend

User Registration & Login

Role-Based Access Control

Procurement Request Creation

Vendor Dashboard

Admin User Management

DTO-based responses

Global Exception Handling

Swagger Documentation

Pagination & Filtering (where applicable)

Frontend

Role-based dashboards

Secure routing

API integration via Axios

Global 401 handling

Clean enterprise UI layout

Login & Registration pages

Dynamic sidebar rendering

🧪 Test Accounts (Seed Data)
Role	Email	Password
ADMIN	admin@smartprocure.com
password
CUSTOMER	customer@smartprocure.com
password
VENDOR	vendor@smartprocure.com
password
🎯 Future Improvements

Refresh Token implementation

Payment workflow expansion

Dashboard analytics

Dockerization

CI/CD pipeline

Multi-tenant support
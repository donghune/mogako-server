# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

Multi-module project with auth-api and calendar-api modules:

- `./gradlew :auth-api:run` - Start the auth-api development server (responds at http://0.0.0.0:8080)
- `./gradlew :calendar-api:run` - Start the calendar-api development server (responds at http://0.0.0.0:8081)
- `./gradlew :auth-api:test` - Run auth-api module tests
- `./gradlew :calendar-api:test` - Run calendar-api module tests
- `./gradlew :auth-api:build` - Build auth-api module
- `./gradlew :calendar-api:build` - Build calendar-api module
- `./gradlew :auth-api:buildFatJar` - Build executable JAR for auth-api
- `./gradlew :calendar-api:buildFatJar` - Build executable JAR for calendar-api
- `./gradlew build` - Build all modules
- `./gradlew test` - Run all tests

## Environment Variables

Required environment variables:
- `JWT_SECRET` - Secret key for JWT token signing (defaults to development key if not set)
- PostgreSQL connection configured in module `application.yaml` files

## Docker Setup

- `docker-compose up` - Start all services (PostgreSQL, auth-api, calendar-api)
- `docker-compose up postgres` - Start only PostgreSQL database
- `docker-compose build` - Build all Docker images
- `./restart.sh` - Complete restart script (rebuild and restart all services)
- `./scripts/restart-services.sh [auth|calendar|db|all]` - Restart specific services
- PostgreSQL runs on port 5432, auth-api on 8080, calendar-api on 8081

### Restart Scripts

- **`./restart.sh`** - Full restart with rebuild (Linux/Mac)
- **`restart.bat`** - Full restart with rebuild (Windows)
- **`./scripts/restart-services.sh auth`** - Restart only auth-api
- **`./scripts/restart-services.sh calendar`** - Restart only calendar-api
- **`./scripts/restart-services.sh db`** - Restart only PostgreSQL

## Architecture Overview

This is a multi-module Kotlin project using Gradle. Currently contains three modules:

### shared Module

A shared library module containing common functionality used across all APIs. Located in `shared/` directory.

#### Shared Components
- **Configuration**: Common HTTP, serialization, database, and OpenAPI configurations
- **JWT Authentication**: Centralized JWT token generation, verification, and authentication setup
- **DTOs**: Common data transfer objects and serializers (LocalDate/LocalDateTime)
- **Database**: Shared database connection and table creation utilities

#### Key Features
- **Code Reuse**: Eliminates duplication between auth-api and calendar-api
- **Consistency**: Ensures consistent configuration across all modules
- **Maintainability**: Single source of truth for common functionality

### auth-api Module

A Ktor-based authentication API using JWT tokens and Google OAuth integration. Located in `auth-api/` directory.

#### Module Structure
- **Application.kt**: Main entry point (`kr.donghune.auth` package)
- **Frameworks.kt**: Dependency injection setup using Koin
- **Databases.kt**: PostgreSQL database configuration using Exposed ORM
- **Security.kt**: JWT authentication configuration
- **Routing.kt**: Main routing setup
- **AuthRoutes.kt**: Authentication API endpoints

#### Authentication System
- **JWT Tokens**: Access tokens (15 min) and refresh tokens (30 days)
- **Google OAuth**: ID token verification for user authentication
- **User Management**: Exposed DAO pattern with Users table and User entity

#### API Endpoints
- `POST /auth/login` - Google OAuth login with ID token
- `POST /auth/refresh` - Refresh access token using refresh token
- `POST /auth/logout` - Logout and invalidate refresh token (requires auth)
- `GET /auth/me` - Get current user info (requires auth)

#### Database Schema
- **Users table**: id, google_id, email, nickname, profile_image_url, refresh_token, created_at, updated_at
- Uses Exposed DAO pattern with plural table objects (`Users`) and singular entity classes (`User`)

#### Key Dependencies
- Ktor 3.1.3 (web framework)
- Exposed 0.61.0 (ORM with DAO support)
- Koin 3.5.6 (dependency injection)
- PostgreSQL driver
- JWT authentication (auth0 JWT library)
- Ktor JWT plugin

### calendar-api Module

A Ktor-based calendar API for personal mood tracking entries. Located in `calendar-api/` directory.

#### Module Structure
- **Application.kt**: Main entry point (`kr.donghune.calendar` package)
- **CalendarEntry.kt**: Domain model with Mood enum (ANGRY, SAD, HAPPY)
- **CalendarService.kt**: CRUD operations for calendar entries
- **CalendarController.kt**: REST API endpoints for calendar management

#### Calendar System
- **CalendarEntry Model**: date, summary, content, mood with timestamps
- **User Isolation**: Each user can only access their own calendar entries
- **JWT Authentication**: Requires valid access token from auth-api

#### API Endpoints
- `GET /calendar` - Get user's calendar entries (optional ?date=YYYY-MM-DD filter)
- `GET /calendar/{id}` - Get specific calendar entry
- `POST /calendar` - Create new calendar entry
- `PUT /calendar/{id}` - Update existing calendar entry
- `DELETE /calendar/{id}` - Delete calendar entry

#### Database Schema
- **CalendarEntries table**: id, user_id, date, summary, content, mood, created_at, updated_at
- Uses Exposed DAO pattern with enum support for Mood field

#### Key Dependencies
- Same as auth-api module (Ktor, Exposed, Koin, PostgreSQL, JWT)
- Custom LocalDate/LocalDateTime serializers for JSON API

## Multi-Module Setup

- Root `build.gradle.kts` contains common configuration
- Each module has its own `build.gradle.kts` with module-specific dependencies
- Use `./gradlew :module-name:task` syntax to run tasks for specific modules
- `shared` module is a dependency for both `auth-api` and `calendar-api`
- Shared configurations eliminate code duplication across modules
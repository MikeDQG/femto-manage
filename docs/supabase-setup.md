# Supabase setup

This project uses Spring Boot as its backend. Supabase is used as a hosted PostgreSQL database; the browser should call the backend, not the database directly. The frontend `.env.example` is only for a future direct Supabase client integration and is not needed for the current architecture.

## 1. Create the project

Create a project at [supabase.com/dashboard](https://supabase.com/dashboard). In **Project Settings → Database**, copy the connection details and database password. Use the session pooler connection if the computer or hosting environment cannot connect over IPv6. Require TLS for hosted connections.

## 2. Configure the backend

Copy `backend-fm/.env.example` to `backend-fm/.env`, then replace the placeholders with the values from Supabase. For the JDBC URL, use the host, port, and database shown in Supabase, with this form:

```text
jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require
```

For a pooler, use its host, port, and username exactly as supplied by Supabase. Do not commit `.env` or share the database password. Spring Boot reads the `SPRING_DATASOURCE_*` environment variables automatically. Run the backend from a shell where those variables are loaded, or configure the IDE run configuration to load `backend-fm/.env` (Spring Boot does not automatically load `.env` files).

Flyway will apply migrations when the backend starts, once migration files exist. If the backend project adds no schema or data migrations, do not let Hibernate create the production schema; use Flyway for schema changes.

## 3. Frontend

The current frontend talks to the Spring Boot API, so it does not need Supabase credentials. Never place the database password, `service_role` key, or any secret in a `VITE_` variable: Vite embeds those values in browser-delivered code. If a future feature uses Supabase directly from the browser, copy `frontend-fm/.env.example` to `frontend-fm/.env.local` and use only the project URL and publishable key. Enable Row Level Security and suitable policies for any tables exposed through Supabase APIs.

## 4. Run locally

With the backend environment variables set, run the Spring Boot application from `backend-fm` using the IDE or:

```powershell
./mvnw.cmd spring-boot:run
```

Run the frontend separately from `frontend-fm` with `npm install` and `npm run dev`. A root Compose setup is not yet configured to load these files.

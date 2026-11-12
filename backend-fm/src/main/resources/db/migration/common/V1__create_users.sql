CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(16) NOT NULL CHECK (role IN ('USER', 'MENTOR', 'ADMIN')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT normalized_email CHECK (email = LOWER(TRIM(email)))
);

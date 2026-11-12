CREATE TABLE daily_reflection (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    date DATE NOT NULL,
    note VARCHAR(4000),
    mood VARCHAR(100),
    energy INTEGER CHECK (energy BETWEEN 1 AND 5),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT reflection_owner_date UNIQUE (user_id, date)
);

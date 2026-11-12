CREATE TABLE category (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE place (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    name VARCHAR(120) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    radius_m INTEGER NOT NULL
);

CREATE TABLE activity (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    category_id UUID NOT NULL REFERENCES category(id),
    place_id UUID REFERENCES place(id),
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    source VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT activity_interval CHECK (end_at > start_at)
);

CREATE INDEX activity_owner_interval ON activity(user_id, start_at, end_at);

INSERT INTO category(id, code, name, active) VALUES
('00000000-0000-0000-0000-000000000001', 'WORK', 'Delo', true),
('00000000-0000-0000-0000-000000000002', 'MOVEMENT', 'Gibanje', true),
('00000000-0000-0000-0000-000000000003', 'GROWTH', 'Osebna rast', true),
('00000000-0000-0000-0000-000000000004', 'RELATIONSHIPS', 'Odnosi', true),
('00000000-0000-0000-0000-000000000005', 'REST', 'Počitek', true),
('00000000-0000-0000-0000-000000000006', 'CREATIVITY', 'Ustvarjanje', true);

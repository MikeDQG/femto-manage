CREATE TABLE daily_summary (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    date DATE NOT NULL,
    category_totals TEXT NOT NULL,
    comment VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE access_grant (
    id UUID PRIMARY KEY,
    summary_id UUID NOT NULL REFERENCES daily_summary(id),
    mentor_id UUID NOT NULL REFERENCES app_user(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT grant_summary_mentor UNIQUE (summary_id, mentor_id)
);
CREATE INDEX summary_owner_date ON daily_summary(user_id, date);
CREATE INDEX grant_mentor ON access_grant(mentor_id, revoked_at);

-- Run after migration locks have been released, including on the first startup.
ALTER TABLE flyway_schema_history ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON flyway_schema_history FROM PUBLIC;
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
        REVOKE ALL ON flyway_schema_history FROM anon;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
        REVOKE ALL ON flyway_schema_history FROM authenticated;
    END IF;
END $$;

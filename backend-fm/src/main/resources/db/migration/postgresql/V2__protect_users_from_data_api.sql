-- Accounts are accessed through Spring Security and JDBC, never the Supabase Data API.
ALTER TABLE app_user ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON app_user FROM PUBLIC;
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
        REVOKE ALL ON app_user FROM anon;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
        REVOKE ALL ON app_user FROM authenticated;
    END IF;
END $$;

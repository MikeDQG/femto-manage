ALTER TABLE daily_reflection ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON daily_reflection FROM anon, authenticated, PUBLIC;

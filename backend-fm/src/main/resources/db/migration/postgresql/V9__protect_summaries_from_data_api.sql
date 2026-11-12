ALTER TABLE daily_summary ENABLE ROW LEVEL SECURITY;
ALTER TABLE access_grant ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON daily_summary, access_grant FROM anon, authenticated, PUBLIC;

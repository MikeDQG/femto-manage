-- Razvojna baza, samo obstojeci testni racun a@a.a in njegov kraj FERI.
-- Opravilo #65; implementacija US-04: #19 in #20.
-- Predpogoj: razvojni podatki #33 za 2026-11-12.
DO $$
DECLARE
    owner_id uuid;
    faculty_id constant uuid := '687d58f4-7e19-33a0-b547-81976e0a6c84';
BEGIN
    SELECT id INTO STRICT owner_id FROM app_user WHERE email = 'a@a.a';
    IF NOT EXISTS (SELECT 1 FROM place WHERE id = faculty_id AND user_id = owner_id AND NOT deleted) THEN
        RAISE EXCEPTION 'Manjka aktivni testni kraj FERI iz razvojnih podatkov za 2026-11-12.';
    END IF;
    IF (SELECT count(*) FROM category WHERE code IN ('WORK', 'GROWTH') AND active) <> 2 THEN
        RAISE EXCEPTION 'Manjkata aktivni kategoriji WORK ali GROWTH.';
    END IF;

    INSERT INTO activity (id, user_id, category_id, place_id, title, description,
                          start_at, end_at, source, status, created_at, updated_at)
    SELECT md5('femto-us04-demo:' || owner_id::text || ':' || sample.day::text)::uuid,
           owner_id, category.id, faculty_id, sample.title,
           'Testni zapis za prikaz US-04 (#19, #20).',
           (sample.day + time '09:00') AT TIME ZONE 'Europe/Ljubljana',
           (sample.day + time '10:00') AT TIME ZONE 'Europe/Ljubljana',
           'MANUAL', 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    FROM (VALUES
        (date '2026-11-05', 'WORK', 'US-04: projektno delo'),
        (date '2026-11-06', 'WORK', 'US-04: projektno delo'),
        (date '2026-11-07', 'WORK', 'US-04: projektno delo'),
        (date '2026-11-08', 'GROWTH', 'US-04: učenje')
    ) AS sample(day, code, title)
    JOIN category ON category.code = sample.code AND category.active
    ON CONFLICT (id) DO NOTHING;
END $$;

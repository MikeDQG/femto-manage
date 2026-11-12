package si.um.feri.riris.backendfm.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.development-data.enabled", havingValue = "true")
public class DevelopmentDataCommand implements CommandLineRunner {
    private static final ZoneId ZONE = ZoneId.of("Europe/Ljubljana");
    private final JdbcTemplate jdbc;
    private final Environment environment;
    private final ApplicationContext context;

    public DevelopmentDataCommand(JdbcTemplate jdbc, Environment environment, ApplicationContext context) {
        this.jdbc = jdbc;
        this.environment = environment;
        this.context = context;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void closeAfterCommand() { SpringApplication.exit(context); }

    record PlaceSpec(String key, String name, double latitude, double longitude, int radius) {}
    record ActivitySpec(String key, int dayOffset, int startHour, int startMinute,
                        int endDayOffset, int endHour, int endMinute,
                        String category, String placeKey, String title) {}

    private static final List<PlaceSpec> PLACES = List.of(
            new PlaceSpec("home", "Dom", 46.5547, 15.6459, 100),
            new PlaceSpec("faculty", "FERI", 46.5593, 15.6382, 120),
            new PlaceSpec("park", "Mestni park", 46.5685, 15.6458, 180));

    private static final List<ActivitySpec> ACTIVITIES = List.of(
            new ActivitySpec("work-today", 0, 9, 0, 0, 11, 0, "WORK", "faculty", "Projektno delo"),
            new ActivitySpec("growth-today", 0, 10, 30, 0, 12, 0, "GROWTH", "faculty", "Učenje"),
            new ActivitySpec("movement-today", 0, 17, 0, 0, 18, 0, "MOVEMENT", "park", "Sprehod"),
            new ActivitySpec("rest-today", 0, 22, 30, 1, 0, 30, "REST", "home", "Večerni počitek"),
            new ActivitySpec("work-yesterday", -1, 9, 0, -1, 11, 0, "WORK", "faculty", "Projektno delo"),
            new ActivitySpec("relationships-yesterday", -1, 18, 0, -1, 19, 0, "RELATIONSHIPS", null, "Pogovor s prijatelji"),
            new ActivitySpec("work-two-days", -2, 9, 0, -2, 11, 0, "WORK", "faculty", "Projektno delo"),
            new ActivitySpec("creativity-two-days", -2, 16, 0, -2, 17, 0, "CREATIVITY", "home", "Ustvarjanje"),
            new ActivitySpec("movement-two-days", -2, 18, 0, -2, 19, 0, "MOVEMENT", "park", "Tek"));

    @Override
    @Transactional
    public void run(String... args) {
        if (!"true".equalsIgnoreCase(environment.getProperty("SEED_CONFIRM_DEVELOPMENT_DATABASE")))
            throw new IllegalStateException("Nastavi SEED_CONFIRM_DEVELOPMENT_DATABASE=true samo za razvojno bazo.");
        String email = environment.getProperty("SEED_USER_EMAIL");
        if (email == null || email.isBlank()) throw new IllegalStateException("Manjka SEED_USER_EMAIL.");
        String action = environment.getProperty("SEED_ACTION", "insert");
        if (!List.of("insert", "remove", "status").contains(action))
            throw new IllegalStateException("SEED_ACTION mora biti insert, remove ali status.");
        String dateText = environment.getProperty("SEED_REFERENCE_DATE");
        LocalDate date = dateText == null || dateText.isBlank() ? LocalDate.now(ZONE) : LocalDate.parse(dateText);
        List<UUID> owners = jdbc.query("SELECT id FROM app_user WHERE email = ?", (rs, row) -> UUID.fromString(rs.getString(1)), email);
        if (owners.isEmpty()) throw new IllegalStateException("Ciljni račun ne obstaja: " + email);
        UUID owner = owners.getFirst();
        int placesChanged = 0;
        int activitiesChanged = 0;
        if (action.equals("insert")) {
            Map<String, UUID> categories = jdbc.query("SELECT id, code FROM category WHERE active = true", (rs, row) ->
                    Map.entry(rs.getString("code"), UUID.fromString(rs.getString("id"))))
                    .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (!categories.keySet().containsAll(List.of("WORK", "MOVEMENT", "GROWTH", "RELATIONSHIPS", "REST", "CREATIVITY")))
                throw new IllegalStateException("Manjkajo zahtevane aktivne kategorije.");
            for (PlaceSpec place : PLACES) {
                UUID id = id(owner, date, "place:" + place.key());
                if (exists("place", id)) continue;
                jdbc.update("INSERT INTO place(id,user_id,name,latitude,longitude,radius_m,deleted) VALUES (?,?,?,?,?,?,false)",
                        id, owner, place.name(), place.latitude(), place.longitude(), place.radius());
                placesChanged++;
            }
            for (ActivitySpec activity : ACTIVITIES) {
                UUID id = id(owner, date, "activity:" + activity.key());
                if (exists("activity", id)) continue;
                UUID placeId = activity.placeKey() == null ? null : id(owner, date, "place:" + activity.placeKey());
                if (placeId != null && !exists("place", placeId))
                    throw new IllegalStateException("Testni kraj manjka; ročno izbrisanega kraja ne obnavljam.");
                var start = local(date, activity.dayOffset(), activity.startHour(), activity.startMinute());
                var end = local(date, activity.endDayOffset(), activity.endHour(), activity.endMinute());
                jdbc.update("INSERT INTO activity(id,user_id,category_id,place_id,title,start_at,end_at,source,status,created_at,updated_at) " +
                                "VALUES (?,?,?,?,?,?,?,'MANUAL','CONFIRMED',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                        id, owner, categories.get(activity.category()), placeId, activity.title(), start, end);
                activitiesChanged++;
            }
        } else if (action.equals("remove")) {
            for (ActivitySpec activity : ACTIVITIES)
                activitiesChanged += jdbc.update("DELETE FROM activity WHERE id = ? AND user_id = ?", id(owner, date, "activity:" + activity.key()), owner);
            for (PlaceSpec place : PLACES)
                placesChanged += jdbc.update("DELETE FROM place WHERE id = ? AND user_id = ? AND NOT EXISTS " +
                                "(SELECT 1 FROM activity WHERE place_id = ?)",
                        id(owner, date, "place:" + place.key()), owner, id(owner, date, "place:" + place.key()));
        }
        System.out.printf("Razvojni podatki: %s, datum %s, kraji %d, aktivnosti %d%n", action, date, placesChanged, activitiesChanged);
        if (!action.equals("remove")) {
            long visiblePlaces = PLACES.stream().filter(p -> Boolean.TRUE.equals(jdbc.queryForObject(
                    "SELECT COUNT(*) > 0 FROM place WHERE id = ? AND user_id = ? AND deleted = false", Boolean.class,
                    id(owner, date, "place:" + p.key()), owner))).count();
            long visibleActivities = ACTIVITIES.stream().filter(a -> Boolean.TRUE.equals(jdbc.queryForObject(
                    "SELECT COUNT(*) > 0 FROM activity WHERE id = ? AND user_id = ? AND status = 'CONFIRMED'", Boolean.class,
                    id(owner, date, "activity:" + a.key()), owner))).count();
            System.out.printf("Vidno v uporabnikovih API poizvedbah: kraji %d, aktivnosti %d%n", visiblePlaces, visibleActivities);
        }
    }

    private boolean exists(String table, UUID id) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT COUNT(*) > 0 FROM " + table + " WHERE id = ?", Boolean.class, id));
    }

    private static UUID id(UUID owner, LocalDate date, String key) {
        return UUID.nameUUIDFromBytes(("femto-issue-33:" + owner + ":" + date + ":" + key).getBytes(StandardCharsets.UTF_8));
    }

    private static java.sql.Timestamp local(LocalDate date, int offset, int hour, int minute) {
        return java.sql.Timestamp.from(LocalDateTime.of(date.plusDays(offset), java.time.LocalTime.of(hour, minute))
                .atZone(ZONE).toInstant());
    }
}

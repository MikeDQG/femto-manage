package si.um.feri.riris.backendfm.api.activity;
import si.um.feri.riris.backendfm.persistence.category.CategoryRepository;
import si.um.feri.riris.backendfm.persistence.category.Category;
import si.um.feri.riris.backendfm.persistence.place.PlaceRepository;
import si.um.feri.riris.backendfm.persistence.activity.ActivityRepository;
import si.um.feri.riris.backendfm.persistence.activity.Activity;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ActivityController {
    private final ActivityRepository activities;
    private final CategoryRepository categories;
    private final PlaceRepository places;
    private final UserRepository users;

    ActivityController(ActivityRepository activities, CategoryRepository categories,
                       PlaceRepository places, UserRepository users) {
        this.activities = activities;
        this.categories = categories;
        this.places = places;
        this.users = users;
    }

    private UUID owner(Authentication auth) {
        return users.findByEmail(auth.getName()).orElseThrow().getId();
    }

    @GetMapping("/categories")
    List<Category> categories() { return categories.findByActiveTrueOrderByName(); }

    @GetMapping("/activities")
    List<Activity> day(@RequestParam LocalDate date, @RequestParam String zone, Authentication auth) {
        ZoneId timezone = ZoneId.of(zone);
        Instant start = date.atStartOfDay(timezone).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(timezone).toInstant();
        return activities.findByUserIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
                owner(auth), "CONFIRMED", end, start);
    }

    @PostMapping("/activities")
    @Transactional
    ResponseEntity<?> create(@RequestBody ActivityRequest request, Authentication auth) {
        UUID ownerId = owner(auth);
        Map<String, String> fields = new LinkedHashMap<>();
        if (request.title() == null || request.title().isBlank()) fields.put("title", "Naziv je obvezen.");
        else if (request.title().trim().length() > 200) fields.put("title", "Naziv je predolg.");
        if (request.startAt() == null) fields.put("startAt", "Začetek je obvezen.");
        if (request.endAt() == null) fields.put("endAt", "Konec je obvezen.");
        if (request.startAt() != null && request.endAt() != null
                && !request.endAt().isAfter(request.startAt())) fields.put("endAt", "Konec mora biti po začetku.");
        if (request.categoryId() == null || categories.findById(request.categoryId()).filter(c -> c.active).isEmpty())
            fields.put("categoryId", "Izberi aktivno kategorijo.");
        if (request.placeId() != null && places.findById(request.placeId())
                .filter(p -> p.userId.equals(ownerId) && !p.deleted).isEmpty()) fields.put("placeId", "Izberi svoj kraj.");
        if (request.description() != null && request.description().length() > 2000)
            fields.put("description", "Opis je predolg.");
        if (!fields.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "Preveri vnesene podatke.", "fields", fields));

        var overlaps = activities.findByUserIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAt(
                ownerId, "CONFIRMED", request.endAt().toInstant(), request.startAt().toInstant());
        if (!overlaps.isEmpty() && !Boolean.TRUE.equals(request.confirmOverlap()))
            return ResponseEntity.status(409).body(Map.of("message", "Aktivnost se prekriva.", "fields", Map.of(),
                    "overlaps", overlaps.stream().map(a -> Map.of("title", a.title, "startAt", a.startAt, "endAt", a.endAt)).toList()));

        Activity activity = new Activity();
        activity.id = UUID.randomUUID();
        activity.userId = ownerId;
        activity.categoryId = request.categoryId();
        activity.placeId = request.placeId();
        activity.title = request.title().trim();
        activity.description = request.description();
        activity.startAt = request.startAt().toInstant();
        activity.endAt = request.endAt().toInstant();
        activity.source = "MANUAL";
        activity.status = "CONFIRMED";
        activity.createdAt = Instant.now();
        activity.updatedAt = activity.createdAt;
        return ResponseEntity.status(201).body(activities.saveAndFlush(activity));
    }
}

package si.um.feri.riris.backendfm.api.activity;
import si.um.feri.riris.backendfm.persistence.category.CategoryRepository;
import si.um.feri.riris.backendfm.persistence.category.Category;
import si.um.feri.riris.backendfm.persistence.activity.ActivityRepository;
import si.um.feri.riris.backendfm.persistence.activity.Activity;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import si.um.feri.riris.backendfm.engine.activity.ActivityService;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ActivityController {
    private final ActivityRepository activities;
    private final CategoryRepository categories;
    private final ActivityService service;
    private final UserRepository users;

    ActivityController(ActivityRepository activities, CategoryRepository categories,
                       ActivityService service, UserRepository users) {
        this.activities = activities;
        this.categories = categories;
        this.service = service;
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

    @GetMapping("/activities/{id}")
    ActivityService.Details get(@PathVariable UUID id, Authentication auth) {
        return service.get(id, owner(auth));
    }

    @PostMapping("/activities")
    ResponseEntity<?> create(@RequestBody ActivityRequest request, Authentication auth) {
        return response(service.save(null, owner(auth), request), 201);
    }

    @PutMapping("/activities/{id}")
    ResponseEntity<?> update(@PathVariable UUID id, @RequestBody ActivityUpdateRequest request, Authentication auth) {
        return response(service.save(id, owner(auth), request.asActivityRequest()), 200);
    }

    private ResponseEntity<?> response(ActivityService.WriteResult result, int successStatus) {
        if (!result.fields().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Preveri vnesene podatke.", "fields", result.fields()));
        if (!result.overlaps().isEmpty())
            return ResponseEntity.status(409).body(Map.of("message", "Aktivnost se prekriva.", "fields", Map.of(), "overlaps", result.overlaps()));
        return ResponseEntity.status(successStatus).body(result.activity());
    }
}

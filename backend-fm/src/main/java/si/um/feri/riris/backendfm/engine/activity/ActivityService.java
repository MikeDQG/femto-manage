package si.um.feri.riris.backendfm.engine.activity;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import si.um.feri.riris.backendfm.api.activity.ActivityRequest;
import si.um.feri.riris.backendfm.persistence.activity.*;
import si.um.feri.riris.backendfm.persistence.category.CategoryRepository;
import si.um.feri.riris.backendfm.persistence.place.PlaceRepository;
import java.time.Instant;
import java.util.*;

@Service
public class ActivityService {
    private final ActivityRepository activities;
    private final CategoryRepository categories;
    private final PlaceRepository places;

    public ActivityService(ActivityRepository activities, CategoryRepository categories, PlaceRepository places) {
        this.activities = activities;
        this.categories = categories;
        this.places = places;
    }

    public record Overlap(String title, Instant startAt, Instant endAt) {}
    public record WriteResult(Activity activity, Map<String, String> fields, List<Overlap> overlaps) {}
    public record Details(UUID id, String title, String description, Instant startAt, Instant endAt,
                          UUID categoryId, String categoryName, UUID placeId, String placeName,
                          String source, String status, Instant createdAt, Instant updatedAt) {}

    private Activity owned(UUID id, UUID owner) {
        return activities.findByIdAndUserIdAndStatus(id, owner, "CONFIRMED")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aktivnost ne obstaja."));
    }

    @Transactional(readOnly = true)
    public Details get(UUID id, UUID owner) {
        Activity activity = owned(id, owner);
        String categoryName = categories.findById(activity.categoryId).map(c -> c.name).orElse("Neznana kategorija");
        String placeName = activity.placeId == null ? null : places.findById(activity.placeId)
                .filter(p -> p.userId.equals(owner)).map(p -> p.name).orElse(null);
        return new Details(activity.id, activity.title, activity.description, activity.startAt, activity.endAt,
                activity.categoryId, categoryName, activity.placeId, placeName, activity.source, activity.status,
                activity.createdAt, activity.updatedAt);
    }

    @Transactional
    public WriteResult save(UUID id, UUID owner, ActivityRequest request) {
        Activity activity = id == null ? new Activity() : owned(id, owner);
        Map<String, String> fields = new LinkedHashMap<>();
        if (request.source() != null && !Set.of("MANUAL", "SUGGESTION", "AI").contains(request.source()))
            fields.put("source", "Neveljaven vir aktivnosti.");
        if (request.title() == null || request.title().isBlank()) fields.put("title", "Naziv je obvezen.");
        else if (request.title().trim().length() > 200) fields.put("title", "Naziv je predolg.");
        if (request.startAt() == null) fields.put("startAt", "Začetek je obvezen.");
        if (request.endAt() == null) fields.put("endAt", "Konec je obvezen.");
        if (request.startAt() != null && request.endAt() != null && !request.endAt().isAfter(request.startAt()))
            fields.put("endAt", "Konec mora biti po začetku.");
        boolean unchangedCategory = id != null && Objects.equals(activity.categoryId, request.categoryId());
        if (request.categoryId() == null || (!unchangedCategory && categories.findById(request.categoryId()).filter(c -> c.active).isEmpty()))
            fields.put("categoryId", "Izberi aktivno kategorijo.");
        boolean unchangedPlace = id != null && Objects.equals(activity.placeId, request.placeId());
        if (request.placeId() != null && places.findById(request.placeId())
                .filter(p -> p.userId.equals(owner) && (!p.deleted || unchangedPlace)).isEmpty())
            fields.put("placeId", "Izberi svoj kraj.");
        if (request.description() != null && request.description().length() > 2000)
            fields.put("description", "Opis je predolg.");
        if (!fields.isEmpty()) return new WriteResult(null, fields, List.of());

        var overlaps = activities.findByUserIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAt(
                owner, "CONFIRMED", request.endAt().toInstant(), request.startAt().toInstant()).stream()
                .filter(other -> !other.id.equals(id)).map(other -> new Overlap(other.title, other.startAt, other.endAt)).toList();
        if (!overlaps.isEmpty() && !Boolean.TRUE.equals(request.confirmOverlap()))
            return new WriteResult(null, Map.of(), overlaps);

        if (id == null) {
            activity.id = UUID.randomUUID();
            activity.userId = owner;
            activity.source = request.source() == null ? "MANUAL" : request.source();
            activity.status = "CONFIRMED";
            activity.createdAt = Instant.now();
        }
        activity.categoryId = request.categoryId();
        activity.placeId = request.placeId();
        activity.title = request.title().trim();
        activity.description = request.description();
        activity.startAt = request.startAt().toInstant();
        activity.endAt = request.endAt().toInstant();
        activity.updatedAt = Instant.now();
        return new WriteResult(activities.saveAndFlush(activity), Map.of(), List.of());
    }
}

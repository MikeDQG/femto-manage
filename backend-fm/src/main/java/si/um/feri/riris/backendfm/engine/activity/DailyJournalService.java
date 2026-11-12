package si.um.feri.riris.backendfm.engine.activity;

import org.springframework.stereotype.Service;
import si.um.feri.riris.backendfm.persistence.activity.Activity;
import si.um.feri.riris.backendfm.persistence.activity.ActivityRepository;
import si.um.feri.riris.backendfm.persistence.category.Category;
import si.um.feri.riris.backendfm.persistence.category.CategoryRepository;
import si.um.feri.riris.backendfm.persistence.place.Place;
import si.um.feri.riris.backendfm.persistence.place.PlaceRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DailyJournalService {
    private final ActivityRepository activities;
    private final CategoryRepository categories;
    private final PlaceRepository places;

    public DailyJournalService(ActivityRepository activities, CategoryRepository categories, PlaceRepository places) {
        this.activities = activities;
        this.categories = categories;
        this.places = places;
    }

    public record DayActivity(UUID id, String title, String description, UUID categoryId, String categoryName,
                              UUID placeId, String placeName, Instant startAt, Instant endAt,
                              Instant segmentStartAt, Instant segmentEndAt, long durationMinutes) {}
    public record CategoryTotal(UUID categoryId, String code, String name, long minutes) {}
    public record Day(LocalDate date, String zone, List<DayActivity> activities, List<CategoryTotal> totals) {}

    public Day day(UUID owner, LocalDate date, ZoneId zone) {
        Instant start = date.atStartOfDay(zone).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();
        List<Activity> entries = activities.findByUserIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
                owner, "CONFIRMED", end, start);
        Map<UUID, Category> categoryById = categories.findAllById(entries.stream().map(a -> a.categoryId).toList())
                .stream().collect(Collectors.toMap(c -> c.id, Function.identity()));
        Map<UUID, Place> placeById = places.findAllById(entries.stream().map(a -> a.placeId).filter(id -> id != null).toList())
                .stream().filter(p -> p.userId.equals(owner)).collect(Collectors.toMap(p -> p.id, Function.identity()));
        Map<UUID, Long> secondsByCategory = new HashMap<>();
        List<DayActivity> blocks = entries.stream().map(entry -> {
            Instant segmentStart = entry.startAt.isBefore(start) ? start : entry.startAt;
            Instant segmentEnd = entry.endAt.isAfter(end) ? end : entry.endAt;
            long seconds = Duration.between(segmentStart, segmentEnd).toSeconds();
            secondsByCategory.merge(entry.categoryId, seconds, Long::sum);
            Category category = categoryById.get(entry.categoryId);
            Place place = placeById.get(entry.placeId);
            return new DayActivity(entry.id, entry.title, entry.description, entry.categoryId,
                    category == null ? "Neznana kategorija" : category.name, entry.placeId,
                    place == null ? null : place.name, entry.startAt, entry.endAt,
                    segmentStart, segmentEnd, seconds / 60);
        }).toList();
        List<CategoryTotal> totals = categories.findByActiveTrueOrderByName().stream()
                .map(category -> new CategoryTotal(category.id, category.code, category.name,
                        secondsByCategory.getOrDefault(category.id, 0L) / 60))
                .toList();
        return new Day(date, zone.getId(), blocks, totals);
    }
}

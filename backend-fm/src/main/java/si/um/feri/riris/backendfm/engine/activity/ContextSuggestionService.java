package si.um.feri.riris.backendfm.engine.activity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.riris.backendfm.api.activity.ActivityDraft;
import si.um.feri.riris.backendfm.engine.place.PlaceService;
import si.um.feri.riris.backendfm.persistence.activity.ActivityRepository;
import si.um.feri.riris.backendfm.persistence.category.CategoryRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContextSuggestionService {
    private final ActivityRepository activities;
    private final CategoryRepository categories;
    private final PlaceService places;

    public ContextSuggestionService(ActivityRepository activities, CategoryRepository categories, PlaceService places) {
        this.activities = activities; this.categories = categories; this.places = places;
    }

    public record Result(String reason, ActivityDraft draft) {}

    @Transactional(readOnly = true)
    public Result suggest(UUID owner, UUID placeId, Instant start, ZoneId zone) {
        places.get(placeId, owner);
        var active = categories.findByActiveTrueOrderByName().stream().map(c -> c.id).collect(Collectors.toSet());
        int hour = start.atZone(zone).getHour();
        var counts = activities.findByUserIdAndPlaceIdAndStatus(owner, placeId, "CONFIRMED").stream()
                .filter(a -> a.startAt.atZone(zone).getHour() == hour && active.contains(a.categoryId))
                .collect(Collectors.groupingBy(a -> a.categoryId, Collectors.counting()));
        long maximum = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        if (maximum == 0) return new Result("Ni podatkov za predlog. Nadaljuj z ročnim vnosom.", null);
        var leaders = counts.entrySet().stream().filter(entry -> entry.getValue() == maximum).toList();
        if (leaders.size() != 1) return new Result("Kategorije so izenačene. Nadaljuj z ročnim vnosom.", null);
        return new Result("Predlog temelji na " + maximum + " potrjenih zapisih na tem kraju v isti uri začetka.",
                new ActivityDraft(null, start, null, leaders.getFirst().getKey(), placeId, "SUGGESTION",
                        Map.of("title", "Dopolni naziv aktivnosti.", "endAt", "Dopolni konec aktivnosti."), List.of()));
    }
}

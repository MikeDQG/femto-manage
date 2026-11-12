package si.um.feri.riris.backendfm.engine.activity;

import org.springframework.stereotype.Service;
import si.um.feri.riris.backendfm.api.activity.ActivityDraft;
import si.um.feri.riris.backendfm.persistence.category.CategoryRepository;
import si.um.feri.riris.backendfm.persistence.place.PlaceRepository;
import java.time.*;
import java.util.*;

@Service
public class TextActivityDraftService {
    private final ActivityTextExtractor extractor;
    private final CategoryRepository categories;
    private final PlaceRepository places;
    public TextActivityDraftService(ActivityTextExtractor extractor, CategoryRepository categories, PlaceRepository places) {
        this.extractor=extractor; this.categories=categories; this.places=places;
    }
    public ActivityDraft draft(UUID owner, String text, LocalDate referenceDate, ZoneId zone) {
        var active=categories.findByActiveTrueOrderByName();
        var extracted=extractor.extract(text, referenceDate, zone, active.stream().map(c -> new ActivityTextExtractor.CategoryChoice(c.code,c.name)).toList());
        Map<String,String> fields=new LinkedHashMap<>();
        String title=extracted.title();
        if (title==null || title.isBlank() || title.length()>200) { title=null; fields.put("title","Dopolni naziv aktivnosti."); }
        Instant start=time(extracted.date(),extracted.startTime(),zone), end=time(extracted.date(),extracted.endTime(),zone);
        if (start==null) fields.put("startAt","Dopolni ali razjasni datum in začetek.");
        if (end==null || (start!=null && !end.isAfter(start))) { end=null; fields.put("endAt","Dopolni ali popravi konec aktivnosti."); }
        UUID category=active.stream().filter(c -> c.code.equals(extracted.categoryCode())).map(c -> c.id).findFirst().orElse(null);
        if (category==null) fields.put("categoryId","Izberi aktivno kategorijo.");
        var candidates=places.findByUserIdAndDeletedFalseOrderByName(owner).stream()
                .filter(p -> extracted.placeName()!=null && p.name.equalsIgnoreCase(extracted.placeName().trim()))
                .map(p -> new ActivityDraft.PlaceCandidate(p.id,p.name)).toList();
        UUID place=candidates.size()==1 ? candidates.getFirst().id() : null;
        if (candidates.size()>1) fields.put("placeId","Več lastnih krajev ustreza opisu. Izberi kraj.");
        else if (extracted.placeName()!=null && candidates.isEmpty()) fields.put("placeId","Opisani kraj ni med tvojimi shranjenimi kraji. Izberi svoj kraj ali nadaljuj brez njega.");
        return new ActivityDraft(title,start,end,category,place,"AI",fields,candidates);
    }
    private Instant time(String date, String time, ZoneId zone) {
        if(date==null || time==null || !date.matches("\\d{4}-\\d{2}-\\d{2}") || !time.matches("\\d{2}:\\d{2}")) return null;
        try {
            LocalDateTime local=LocalDate.parse(date).atTime(LocalTime.parse(time));
            var offsets=zone.getRules().getValidOffsets(local);
            return offsets.size()==1 ? local.toInstant(offsets.getFirst()) : null;
        } catch(DateTimeException exception) { return null; }
    }
}

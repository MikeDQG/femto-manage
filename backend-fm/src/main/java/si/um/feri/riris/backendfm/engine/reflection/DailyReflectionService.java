package si.um.feri.riris.backendfm.engine.reflection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.riris.backendfm.api.reflection.ReflectionRequest;
import si.um.feri.riris.backendfm.persistence.reflection.*;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;
import java.time.*;
import java.util.UUID;

@Service
public class DailyReflectionService {
    private final DailyReflectionRepository reflections;
    private final UserRepository users;
    public DailyReflectionService(DailyReflectionRepository reflections, UserRepository users) { this.reflections=reflections; this.users=users; }
    public record Reflection(LocalDate date, String note, String mood, Integer energy, Instant createdAt, Instant updatedAt) {}
    private Reflection response(DailyReflection reflection) {
        return new Reflection(reflection.date,reflection.note,reflection.mood,reflection.energy,reflection.createdAt,reflection.updatedAt);
    }
    @Transactional(readOnly=true)
    public Reflection get(UUID owner, LocalDate date) {
        return reflections.findByUserIdAndDate(owner,date).map(this::response).orElse(new Reflection(date,null,null,null,null,null));
    }
    @Transactional
    public Reflection save(UUID owner, LocalDate date, ReflectionRequest input) {
        // Serialize creation for the same owner before checking the unique owner/day pair.
        users.lockById(owner).orElseThrow();
        DailyReflection reflection=reflections.findByUserIdAndDate(owner,date).orElseGet(() -> {
            DailyReflection fresh=new DailyReflection(); fresh.id=UUID.randomUUID(); fresh.userId=owner;
            fresh.date=date; fresh.createdAt=Instant.now(); return fresh;
        });
        reflection.note=input.note(); reflection.mood=input.mood();
        reflection.energy=input.energy()==null ? null : input.energy().intValueExact(); reflection.updatedAt=Instant.now();
        return response(reflections.saveAndFlush(reflection));
    }
}

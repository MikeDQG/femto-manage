package si.um.feri.riris.backendfm.api.reflection;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import si.um.feri.riris.backendfm.engine.reflection.DailyReflectionService;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/journal/reflection")
public class DailyReflectionController {
    private final DailyReflectionService reflections;
    private final UserRepository users;
    public DailyReflectionController(DailyReflectionService reflections, UserRepository users) { this.reflections=reflections; this.users=users; }
    @GetMapping
    DailyReflectionService.Reflection get(@RequestParam LocalDate date, Authentication auth) {
        return reflections.get(users.findByEmail(auth.getName()).orElseThrow().getId(),date);
    }
    @PutMapping
    DailyReflectionService.Reflection save(@RequestParam LocalDate date, @Valid @RequestBody ReflectionRequest input, Authentication auth) {
        return reflections.save(users.findByEmail(auth.getName()).orElseThrow().getId(),date,input);
    }
}

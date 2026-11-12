package si.um.feri.riris.backendfm.api.activity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import si.um.feri.riris.backendfm.engine.activity.ContextSuggestionService;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;

import java.time.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/activities/context-suggestion")
public class ContextSuggestionController {
    private final ContextSuggestionService suggestions;
    private final UserRepository users;
    public ContextSuggestionController(ContextSuggestionService suggestions, UserRepository users) {
        this.suggestions = suggestions; this.users = users;
    }
    public record Input(@NotNull UUID placeId, @NotNull Instant startAt, @NotNull String zone) {}

    @PostMapping
    ContextSuggestionService.Result suggest(@Valid @RequestBody Input input, Authentication auth) {
        ZoneId zone;
        try { zone = ZoneId.of(input.zone()); }
        catch (DateTimeException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Neveljaven časovni pas."); }
        return suggestions.suggest(users.findByEmail(auth.getName()).orElseThrow().getId(), input.placeId(), input.startAt(), zone);
    }
}

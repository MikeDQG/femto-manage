package si.um.feri.riris.backendfm.api.activity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import si.um.feri.riris.backendfm.engine.activity.TextActivityDraftService;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;
import java.time.*;

@RestController
@RequestMapping("/api/activities/text-draft")
public class TextActivityDraftController {
    private final TextActivityDraftService drafts;
    private final UserRepository users;
    public TextActivityDraftController(TextActivityDraftService drafts, UserRepository users) { this.drafts=drafts; this.users=users; }
    public record Input(@NotBlank @Size(max=4000) String text, @NotNull LocalDate referenceDate, @NotNull String zone) {}
    @PostMapping
    ActivityDraft draft(@Valid @RequestBody Input input, Authentication auth) {
        ZoneId zone;
        try { zone=ZoneId.of(input.zone()); }
        catch(DateTimeException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Neveljaven časovni pas."); }
        return drafts.draft(users.findByEmail(auth.getName()).orElseThrow().getId(),input.text(),input.referenceDate(),zone);
    }
}

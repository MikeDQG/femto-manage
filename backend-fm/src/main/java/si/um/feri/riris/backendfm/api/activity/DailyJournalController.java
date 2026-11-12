package si.um.feri.riris.backendfm.api.activity;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import si.um.feri.riris.backendfm.engine.activity.DailyJournalService;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/journal")
public class DailyJournalController {
    private final DailyJournalService journal;
    private final UserRepository users;

    public DailyJournalController(DailyJournalService journal, UserRepository users) {
        this.journal = journal;
        this.users = users;
    }

    @GetMapping("/day")
    public DailyJournalService.Day day(@RequestParam LocalDate date, @RequestParam String zone, Authentication authentication) {
        try {
            ZoneId timezone = ZoneId.of(zone);
            return journal.day(users.findByEmail(authentication.getName()).orElseThrow().getId(), date, timezone);
        } catch (DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Neveljaven časovni pas ali datum.");
        }
    }
}

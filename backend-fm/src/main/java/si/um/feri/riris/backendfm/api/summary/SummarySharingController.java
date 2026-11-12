package si.um.feri.riris.backendfm.api.summary;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import si.um.feri.riris.backendfm.engine.summary.SummarySharingService;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/summaries")
public class SummarySharingController {
    private static final String PREVIEW=SummarySharingController.class.getName()+".preview";
    private final SummarySharingService sharing;
    private final UserRepository users;
    public SummarySharingController(SummarySharingService sharing,UserRepository users) { this.sharing=sharing; this.users=users; }
    @ModelAttribute
    void noCache(HttpServletResponse response) { response.setHeader("Cache-Control","no-store"); }
    private UUID owner(Authentication auth) { return users.findByEmail(auth.getName()).orElseThrow().getId(); }
    public record PreviewInput(@NotNull LocalDate date,@NotNull String zone,@NotNull UUID mentorId,@Size(max=2000) String comment) {}
    public record ConfirmInput(@NotNull UUID previewId,@NotNull @AssertTrue(message="Deljenje izrecno potrdi.") Boolean confirmed) {}
    public record Preview(UUID previewId,SummarySharingService.Mentor mentor,SummarySharingService.Snapshot content) {}
    @GetMapping("/mentors")
    List<SummarySharingService.Mentor> mentors() { return sharing.mentors(); }
    @PostMapping("/preview")
    Preview preview(@Valid @RequestBody PreviewInput input,Authentication auth,HttpSession session) {
        ZoneId zone;
        try { zone=ZoneId.of(input.zone()); }
        catch(DateTimeException exception) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Neveljaven časovni pas."); }
        var pending=sharing.preview(owner(auth),input.date(),zone,input.mentorId(),input.comment());
        synchronized(session) { session.setAttribute(PREVIEW,pending); }
        var mentor=sharing.mentors().stream().filter(m -> m.id().equals(input.mentorId())).findFirst().orElseThrow();
        return new Preview(pending.id(),mentor,pending.snapshot());
    }
    @PostMapping("/shares")
    @ResponseStatus(HttpStatus.CREATED)
    SummarySharingService.Share confirm(@Valid @RequestBody ConfirmInput input,Authentication auth,HttpSession session) {
        synchronized(session) {
            var pending=(SummarySharingService.Pending)session.getAttribute(PREVIEW);
            if(pending==null || !pending.id().equals(input.previewId())) throw sharing.stale();
            var share=sharing.confirm(owner(auth),pending);
            session.removeAttribute(PREVIEW);
            return share;
        }
    }
    @GetMapping("/shares")
    List<SummarySharingService.Share> shares(Authentication auth) { return sharing.shares(owner(auth)); }
    @DeleteMapping("/shares/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revoke(@PathVariable UUID id,Authentication auth) { sharing.revoke(owner(auth),id); }
    @GetMapping("/{id}")
    SummarySharingService.Snapshot read(@PathVariable UUID id,Authentication auth) { return sharing.read(owner(auth),id); }
}

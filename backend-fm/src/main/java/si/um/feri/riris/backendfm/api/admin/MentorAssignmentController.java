package si.um.feri.riris.backendfm.api.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import si.um.feri.riris.backendfm.engine.admin.MentorAssignmentService;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;
import java.util.Locale;

@RestController
@RequestMapping("/api/admin/accounts")
public class MentorAssignmentController {
    private final MentorAssignmentService assignments;
    private final UserRepository users;
    public MentorAssignmentController(MentorAssignmentService assignments,UserRepository users) { this.assignments=assignments; this.users=users; }
    private static String normalize(String email) { return email==null ? null : email.strip().toLowerCase(Locale.ROOT); }
    public record LookupInput(@NotBlank @Email @Size(max=254) String email) {
        public LookupInput { email=normalize(email); }
    }
    public record AssignmentInput(@NotBlank @Email @Size(max=254) String email,
                                  @NotNull @AssertTrue(message="Dodelitev mentorja izrecno potrdi.") Boolean confirmed) {
        public AssignmentInput { email=normalize(email); }
    }
    @GetMapping
    MentorAssignmentService.Lookup lookup(@Valid @ModelAttribute LookupInput input,Authentication auth) {
        return assignments.find(users.findByEmail(auth.getName()).orElseThrow().getId(),input.email());
    }
    @PostMapping("/mentor")
    MentorAssignmentService.Assignment assign(@Valid @RequestBody AssignmentInput input,Authentication auth) {
        return assignments.assign(users.findByEmail(auth.getName()).orElseThrow().getId(),input.email());
    }
}

package si.um.feri.riris.backendfm.engine.admin;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import si.um.feri.riris.backendfm.persistence.user.*;
import java.util.UUID;

@Service
public class MentorAssignmentService {
    private final UserRepository users;
    public MentorAssignmentService(UserRepository users) { this.users=users; }
    public record Account(String email,User.Role role) {}
    public record Lookup(boolean found,Account account) {}
    public record Assignment(Account account,boolean changed) {}
    private void requireAdmin(UUID actor) {
        if(users.findById(actor).orElseThrow().getRole()!=User.Role.ADMIN)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Dodelitev je na voljo samo skrbniku.");
    }
    @Transactional(readOnly=true)
    public Lookup find(UUID actor,String email) {
        requireAdmin(actor);
        return users.findByEmail(email).map(u -> new Lookup(true,new Account(u.getEmail(),u.getRole()))).orElse(new Lookup(false,null));
    }
    @Transactional
    public Assignment assign(UUID actor,String email) {
        requireAdmin(actor);
        UUID id=users.findByEmail(email).map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Račun s tem naslovom ne obstaja."));
        User account=users.lockById(id).orElseThrow();
        if(account.getRole()==User.Role.ADMIN) throw new ResponseStatusException(HttpStatus.CONFLICT,"Skrbniškega računa ni mogoče spremeniti v mentorja.");
        boolean changed=account.getRole()==User.Role.USER;
        account.assignMentor(); users.saveAndFlush(account);
        return new Assignment(new Account(account.getEmail(),account.getRole()),changed);
    }
}

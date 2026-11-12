package si.um.feri.riris.backendfm.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationService {
    private final UserRepository users;
    private final PasswordEncoder passwords;

    public RegistrationService(UserRepository users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    public UserResponse register(RegistrationRequest request) {
        if (users.existsByEmail(request.email())) {
            throw duplicateEmail();
        }
        try {
            return UserResponse.from(users.saveAndFlush(new User(request.email(),
                    passwords.encode(request.password()), User.Role.USER)));
        } catch (DataIntegrityViolationException exception) {
            // The unique constraint also handles simultaneous requests for the same email.
            throw duplicateEmail();
        }
    }

    private ResponseStatusException duplicateEmail() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Račun s tem e-poštnim naslovom že obstaja.");
    }
}

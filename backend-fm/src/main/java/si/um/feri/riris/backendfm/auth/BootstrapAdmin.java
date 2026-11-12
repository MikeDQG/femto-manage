package si.um.feri.riris.backendfm.auth;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdmin implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final Validator validator;
    private final String email;
    private final String password;

    public BootstrapAdmin(UserRepository users, PasswordEncoder passwords, Validator validator,
                          @Value("${app.bootstrap-admin.email:}") String email,
                          @Value("${app.bootstrap-admin.password:}") String password) {
        this.users = users;
        this.passwords = passwords;
        this.validator = validator;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        if (email.isEmpty() && password.isEmpty()) return;
        var credentials = new RegistrationRequest(email, password, null);
        if (!validator.validate(credentials).isEmpty()) {
            throw new IllegalStateException("Za začetnega skrbnika nastavi veljaven BOOTSTRAP_ADMIN_EMAIL in BOOTSTRAP_ADMIN_PASSWORD (vsaj 12 znakov, največ 72 bajtov UTF-8).");
        }
        var existing = users.findByEmail(credentials.email());
        if (existing.isPresent()) {
            if (existing.get().getRole() != User.Role.ADMIN) {
                throw new IllegalStateException("E-pošta začetnega skrbnika že pripada navadnemu računu. Izberi drug naslov.");
            }
            return;
        }
        users.saveAndFlush(new User(credentials.email(), passwords.encode(password), User.Role.ADMIN));
    }
}

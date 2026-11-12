package si.um.feri.riris.backendfm.bootstrap;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;
import si.um.feri.riris.backendfm.persistence.user.User;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BootstrapAdminTests {
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired Validator validator;

    @Test
    void createsAdminOnceWithoutResettingPassword() {
        var bootstrap = new BootstrapAdmin(users, passwords, validator, " Admin@Example.com ", "long-password-123");
        bootstrap.run(null);
        var admin = users.findByEmail("admin@example.com").orElseThrow();
        assertThat(admin.getRole()).isEqualTo(User.Role.ADMIN);
        assertThat(passwords.matches("long-password-123", admin.getPasswordHash())).isTrue();
        bootstrap.run(null);
        assertThat(users.findByEmail("admin@example.com").orElseThrow().getPasswordHash()).isEqualTo(admin.getPasswordHash());
    }

    @Test
    void refusesPrivilegeEscalationAndIncompleteConfiguration() {
        var user = users.saveAndFlush(new User("ordinary@example.com", passwords.encode("long-password-123"), User.Role.USER));
        assertThatThrownBy(() -> new BootstrapAdmin(users, passwords, validator,
                user.getEmail(), "long-password-123").run(null)).isInstanceOf(IllegalStateException.class);
        assertThat(users.findById(user.getId()).orElseThrow().getRole()).isEqualTo(User.Role.USER);
        assertThatThrownBy(() -> new BootstrapAdmin(users, passwords, validator,
                "missing@example.com", "").run(null)).isInstanceOf(IllegalStateException.class);
        long count = users.count();
        new BootstrapAdmin(users, passwords, validator, "", "").run(null);
        assertThat(users.count()).isEqualTo(count);
    }
}

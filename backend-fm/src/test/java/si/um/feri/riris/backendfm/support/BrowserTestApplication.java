package si.um.feri.riris.backendfm.support;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import si.um.feri.riris.backendfm.BackendFmApplication;
import si.um.feri.riris.backendfm.persistence.user.*;

public class BrowserTestApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(BackendFmApplication.class,Fixtures.class).profiles("test")
                .run("--server.port=8081","--spring.config.import=","--app.ai.api-key=","--app.ai.model=",
                        "--logging.level.root=WARN","--spring.main.banner-mode=off","--debug=false");
    }
    @TestConfiguration
    public static class Fixtures {
        @Bean
        ApplicationRunner administrator(UserRepository users,PasswordEncoder passwords) {
            return args -> {
                users.saveAndFlush(new User("browser-admin@example.test",passwords.encode("test-only-password-123"),User.Role.ADMIN));
            };
        }
    }
}

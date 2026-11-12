package si.um.feri.riris.backendfm.api.auth;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;

    @Test
    void registersNormalizedUserWithHashAndRejectsDuplicate() throws Exception {
        String body = "{\"email\":\"  Register@Example.com \",\"password\":\"long-password-123\"}";
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.email").value("register@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        var user = users.findByEmail("register@example.com").orElseThrow();
        assertThat(passwords.matches("long-password-123", user.getPasswordHash())).isTrue();
        assertThat(user.getCreatedAt()).isNotNull();
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isConflict());
        assertThat(users.findAll().stream().filter(u -> u.getEmail().equals("register@example.com"))).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"email\":\"bad\",\"password\":\"long-password-123\"}",
        "{\"email\":\"bad@example.com\",\"password\":\"short\"}",
        "{\"email\":\"bad@example.com\",\"password\":\"            \"}",
        "{\"email\":\"bad@example.com\",\"password\":\"long-password-123\",\"role\":\"ADMIN\"}",
        "{\"email\":\"bad@example.com\",\"password\":\"long-password-123\",\"role\":\"MENTOR\"}",
        "{}"
    })
    void rejectsInvalidInputWithoutCreatingAccount(String body) throws Exception {
        long count = users.count();
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields").isNotEmpty());
        assertThat(users.count()).isEqualTo(count);
    }

    @Test
    void rejectsOverlongUtf8Password() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json")
                .content("{\"email\":\"utf@example.com\",\"password\":\"" + "ž".repeat(37) + "\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    void requiresCsrfAndProtectsPrivateRoutes() throws Exception {
        mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
        mvc.perform(post("/api/auth/register").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/private")).andExpect(status().isUnauthorized());
    }
}

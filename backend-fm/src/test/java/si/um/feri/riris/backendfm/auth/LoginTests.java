package si.um.feri.riris.backendfm.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;

    private User createUser() {
        return users.saveAndFlush(new User(java.util.UUID.randomUUID() + "@example.com",
                passwords.encode("long-password-123"), User.Role.USER));
    }

    private String credentials(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private MvcResult login(User user, MockHttpSession session) throws Exception {
        return mvc.perform(post("/api/auth/login").session(session).with(request -> {
                    request.setRequestedSessionId(session.getId());
                    request.setRequestedSessionIdValid(true);
                    return request;
                }).with(csrf()).contentType("application/json")
                .content(credentials(user.getEmail().toUpperCase(java.util.Locale.ROOT), "long-password-123")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.passwordHash").doesNotExist()).andReturn();
    }

    @Test
    void authenticatesRotatesSessionAndReturnsOnlyCurrentAccount() throws Exception {
        var user = createUser();
        var other = createUser();
        var session = new MockHttpSession();
        String previousId = session.getId();
        login(user, session);
        assertThat(session.getId()).isNotEqualTo(previousId);
        mvc.perform(get("/api/auth/me").session(session).param("userId", other.getId().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.role").value("USER"));
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        var otherSession = new MockHttpSession();
        login(other, otherSession);
        mvc.perform(get("/api/auth/me").session(otherSession)).andExpect(jsonPath("$.email").value(other.getEmail()));
        mvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void wrongPasswordAndUnknownAccountHaveSameErrorAndNoAuthenticatedSession() throws Exception {
        var user = createUser();
        var session = new MockHttpSession();
        String wrong = mvc.perform(post("/api/auth/login").session(session).with(csrf())
                .contentType("application/json").content(credentials(user.getEmail(), "wrong-password")))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String unknown = mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content(credentials("unknown@example.com", "wrong-password")))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        assertThat(wrong).isEqualTo(unknown);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void loginAndLogoutRequireCsrfAndLogoutInvalidatesSession() throws Exception {
        var user = createUser();
        mvc.perform(post("/api/auth/login").contentType("application/json")
                .content(credentials(user.getEmail(), "long-password-123"))).andExpect(status().isForbidden());
        var session = new MockHttpSession();
        login(user, session);
        mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMalformedAndInvalidLoginInput() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.email").exists());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content("{"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content(credentials("valid@example.com", "ž".repeat(37)))).andExpect(status().isBadRequest());
    }
}

package si.um.feri.riris.backendfm.api.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import si.um.feri.riris.backendfm.persistence.user.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class MentorAssignmentTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    private User account(User.Role role) { return users.saveAndFlush(new User(UUID.randomUUID()+"@test.example",passwords.encode("long-password-123"),role)); }
    private String input(User target,boolean confirmed) { return "{\"email\":\""+target.getEmail().toUpperCase(Locale.ROOT)+"\",\"confirmed\":"+confirmed+"}"; }
    @Test void exactLookupExplicitAssignmentAndRepetitionReturnOnlyPublicFields() throws Exception {
        User admin=account(User.Role.ADMIN), target=account(User.Role.USER);
        long count=users.count();
        String lookup=mvc.perform(get("/api/admin/accounts").param("email"," "+target.getEmail().toUpperCase(Locale.ROOT)+" ").with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.found").value(true)).andExpect(jsonPath("$.account.role").value("USER"))
                .andReturn().getResponse().getContentAsString();
        assertThat(lookup).doesNotContain("password","createdAt","id","activity","reflection");
        mvc.perform(get("/api/admin/accounts").param("email",UUID.randomUUID()+"@test.example").with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.found").value(false));
        mvc.perform(post("/api/admin/accounts/mentor").with(user(admin.getEmail()).roles("ADMIN")).with(csrf()).contentType("application/json").content(input(target,false))).andExpect(status().isBadRequest());
        assertThat(users.findById(target.getId()).orElseThrow().getRole()).isEqualTo(User.Role.USER);
        mvc.perform(post("/api/admin/accounts/mentor").with(user(admin.getEmail()).roles("ADMIN")).with(csrf()).contentType("application/json").content(input(target,true)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.changed").value(true)).andExpect(jsonPath("$.account.role").value("MENTOR"));
        mvc.perform(post("/api/admin/accounts/mentor").with(user(admin.getEmail()).roles("ADMIN")).with(csrf()).contentType("application/json").content(input(target,true)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.changed").value(false));
        assertThat(users.count()).isEqualTo(count);
        var session=new MockHttpSession();
        mvc.perform(post("/api/auth/login").session(session).with(csrf()).contentType("application/json").content("{\"email\":\""+target.getEmail()+"\",\"password\":\"long-password-123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("MENTOR"));
        mvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.role").value("MENTOR"));
    }
    @Test void onlyAdminCanSearchAndAssignEvenThroughDirectApi() throws Exception {
        User target=account(User.Role.USER);
        for(User.Role role:List.of(User.Role.USER,User.Role.MENTOR)) {
            User actor=account(role);
            mvc.perform(get("/api/admin/accounts").param("email",target.getEmail()).with(user(actor.getEmail()).roles(role.name()))).andExpect(status().isForbidden());
            mvc.perform(post("/api/admin/accounts/mentor").with(user(actor.getEmail()).roles(role.name())).with(csrf()).contentType("application/json").content(input(target,true))).andExpect(status().isForbidden());
        }
        mvc.perform(get("/api/admin/accounts").param("email",target.getEmail())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/accounts/mentor").with(csrf()).contentType("application/json").content(input(target,true))).andExpect(status().isUnauthorized());
        assertThat(users.findById(target.getId()).orElseThrow().getRole()).isEqualTo(User.Role.USER);
    }
    @Test void validatesInputMissingAccountsAdminTargetsAndCsrf() throws Exception {
        User admin=account(User.Role.ADMIN), target=account(User.Role.USER);
        mvc.perform(get("/api/admin/accounts").param("email","invalid").with(user(admin.getEmail()).roles("ADMIN"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/accounts/mentor").with(user(admin.getEmail()).roles("ADMIN")).contentType("application/json").content(input(target,true))).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/accounts/mentor").with(user(admin.getEmail()).roles("ADMIN")).with(csrf()).contentType("application/json").content(input(admin,true))).andExpect(status().isConflict());
        mvc.perform(post("/api/admin/accounts/mentor").with(user(admin.getEmail()).roles("ADMIN")).with(csrf()).contentType("application/json").content("{\"email\":\"missing@test.example\",\"confirmed\":true}"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content("{\"email\":\"new@test.example\",\"password\":\"long-password-123\",\"role\":\"MENTOR\"}"))
                .andExpect(status().isBadRequest());
    }
}

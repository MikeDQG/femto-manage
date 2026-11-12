package si.um.feri.riris.backendfm.api.reflection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import si.um.feri.riris.backendfm.persistence.reflection.DailyReflectionRepository;
import si.um.feri.riris.backendfm.persistence.user.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class DailyReflectionTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired DailyReflectionRepository reflections;
    private User account(User.Role role) { return users.saveAndFlush(new User(UUID.randomUUID()+"@test.example","hash",role)); }
    private static final String URL="/api/journal/reflection?date=2026-11-12";
    @Test void createsReadsUpdatesOneReflectionAndKeepsJournalUnchanged() throws Exception {
        User owner=account(User.Role.USER);
        String before=mvc.perform(get("/api/journal/day?date=2026-11-12&zone=Europe/Ljubljana").with(user(owner.getEmail()))).andReturn().getResponse().getContentAsString();
        long count=reflections.count();
        mvc.perform(put(URL).with(user(owner.getEmail())).with(csrf()).contentType("application/json").content("{\"note\":\"Zasebno\",\"mood\":\"Dobro\",\"energy\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.energy").value(1)).andExpect(jsonPath("$.createdAt").exists());
        mvc.perform(get(URL).with(user(owner.getEmail()))).andExpect(jsonPath("$.note").value("Zasebno")).andExpect(jsonPath("$.energy").value(1));
        mvc.perform(put(URL).with(user(owner.getEmail())).with(csrf()).contentType("application/json").content("{\"note\":\"Sprememba\",\"energy\":5}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.energy").value(5)).andExpect(jsonPath("$.note").value("Sprememba"));
        assertThat(reflections.count()).isEqualTo(count+1);
        mvc.perform(get(URL.replace("12","13")).with(user(owner.getEmail()))).andExpect(jsonPath("$.note").isEmpty());
        String after=mvc.perform(get("/api/journal/day?date=2026-11-12&zone=Europe/Ljubljana").with(user(owner.getEmail()))).andReturn().getResponse().getContentAsString();
        assertThat(after).isEqualTo(before);
        mvc.perform(put(URL).with(user(owner.getEmail())).with(csrf()).contentType("application/json").content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.energy").isEmpty());
    }
    @Test void rejectsFractionalOutOfRangeOwnershipAndUnauthenticatedWrites() throws Exception {
        User owner=account(User.Role.USER);
        for(String energy : new String[]{"0","6","2.5","-1","999999999999"}) {
            mvc.perform(put(URL).with(user(owner.getEmail())).with(csrf()).contentType("application/json").content("{\"energy\":"+energy+"}"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.energy").exists());
        }
        mvc.perform(put(URL).with(user(owner.getEmail())).with(csrf()).contentType("application/json").content("{\"userId\":\""+owner.getId()+"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put(URL).with(csrf()).contentType("application/json").content("{}" )).andExpect(status().isUnauthorized());
        mvc.perform(put(URL).with(user(owner.getEmail())).contentType("application/json").content("{}" )).andExpect(status().isForbidden());
    }
    @Test void allOtherRolesOnlyAccessOwnReflectionEvenWithOwnerQuery() throws Exception {
        User owner=account(User.Role.USER);
        mvc.perform(put(URL).with(user(owner.getEmail())).with(csrf()).contentType("application/json").content("{\"note\":\"Skrivnost\"}" )).andExpect(status().isOk());
        for(User.Role role : User.Role.values()) {
            User other=account(role);
            mvc.perform(get(URL+"&userId="+owner.getId()).with(user(other.getEmail()).roles(role.name())))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.note").isEmpty());
            mvc.perform(put(URL).with(user(other.getEmail()).roles(role.name())).with(csrf()).contentType("application/json").content("{\"note\":\"Moja\"}" )).andExpect(status().isOk());
        }
        mvc.perform(get(URL).with(user(owner.getEmail()))).andExpect(jsonPath("$.note").value("Skrivnost"));
        mvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }
}

package si.um.feri.riris.backendfm.api.activity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import si.um.feri.riris.backendfm.persistence.activity.*;
import si.um.feri.riris.backendfm.persistence.place.*;
import si.um.feri.riris.backendfm.persistence.user.*;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ContextSuggestionTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PlaceRepository places;
    @Autowired ActivityRepository activities;
    private User account() { return users.saveAndFlush(new User(UUID.randomUUID()+"@example.test", "hash", User.Role.USER)); }
    private Place place(User owner) {
        Place place = new Place(); place.id = UUID.randomUUID(); place.userId = owner.getId(); place.name = "Dom"; place.radiusM = 100;
        return places.saveAndFlush(place);
    }
    private void activity(User owner, Place place, int category, String status, String time) {
        Activity a = new Activity(); a.id=UUID.randomUUID(); a.userId=owner.getId(); a.placeId=place.id;
        a.categoryId=UUID.fromString("00000000-0000-0000-0000-00000000000"+category); a.title="Zgodovina";
        a.startAt=Instant.parse(time); a.endAt=a.startAt.plusSeconds(3600); a.source="MANUAL"; a.status=status;
        a.createdAt=Instant.now(); a.updatedAt=a.createdAt; activities.saveAndFlush(a);
    }
    private String input(Place place, String zone) {
        return "{\"placeId\":\""+place.id+"\",\"startAt\":\"2026-11-12T08:30:00Z\",\"zone\":\""+zone+"\"}";
    }
    @Test void countsOnlyConfirmedOwnerPlaceAndLocalHourWithoutWriting() throws Exception {
        User owner=account(), other=account(); Place place=place(owner), elsewhere=place(owner);
        activity(owner,place,1,"CONFIRMED","2026-07-12T07:00:00Z");
        activity(owner,place,1,"CONFIRMED","2026-11-11T08:59:00Z");
        activity(owner,place,2,"CONFIRMED","2026-11-11T08:00:00Z");
        for(int i=0;i<3;i++) {
            activity(owner,place,2,"DRAFT","2026-11-11T08:00:00Z");
            activity(owner,place,2,"REJECTED","2026-11-11T08:00:00Z");
            activity(other,place,2,"CONFIRMED","2026-11-11T08:00:00Z");
            activity(owner,elsewhere,2,"CONFIRMED","2026-11-11T08:00:00Z");
            activity(owner,place,2,"CONFIRMED","2026-11-11T10:00:00Z");
        }
        long before=activities.count();
        mvc.perform(post("/api/activities/context-suggestion").with(user(owner.getEmail())).with(csrf())
                .contentType("application/json").content(input(place,"Europe/Ljubljana")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.draft.categoryId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.draft.title").isEmpty()).andExpect(jsonPath("$.draft.endAt").isEmpty())
                .andExpect(jsonPath("$.draft.fields.title").exists()).andExpect(jsonPath("$.draft.source").value("SUGGESTION"));
        assertThat(activities.count()).isEqualTo(before);
    }
    @Test void emptyTieOwnershipAuthenticationAndInvalidZone() throws Exception {
        User owner=account(), other=account(); Place place=place(owner);
        mvc.perform(post("/api/activities/context-suggestion").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content(input(place,"Europe/Ljubljana")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.draft").isEmpty());
        activity(owner,place,1,"CONFIRMED","2026-11-11T08:00:00Z");
        activity(owner,place,2,"CONFIRMED","2026-11-11T08:00:00Z");
        mvc.perform(post("/api/activities/context-suggestion").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content(input(place,"Europe/Ljubljana")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.draft").isEmpty()).andExpect(jsonPath("$.reason").value("Kategorije so izenačene. Nadaljuj z ročnim vnosom."));
        mvc.perform(post("/api/activities/context-suggestion").with(user(other.getEmail())).with(csrf()).contentType("application/json").content(input(place,"Europe/Ljubljana"))).andExpect(status().isNotFound());
        mvc.perform(post("/api/activities/context-suggestion").with(csrf()).contentType("application/json").content(input(place,"Europe/Ljubljana"))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/activities/context-suggestion").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content(input(place,"bad"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/activities/context-suggestion").with(user(owner.getEmail())).contentType("application/json").content(input(place,"Europe/Ljubljana"))).andExpect(status().isForbidden());
    }
}

package si.um.feri.riris.backendfm.api.place;
import si.um.feri.riris.backendfm.persistence.place.PlaceRepository;
import si.um.feri.riris.backendfm.persistence.place.Place;
import si.um.feri.riris.backendfm.persistence.activity.ActivityRepository;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;
import si.um.feri.riris.backendfm.persistence.user.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class PlaceTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PlaceRepository places;
    @Autowired ActivityRepository activities;
    String account() {
        String email = UUID.randomUUID() + "@example.com";
        users.save(new User(email, "hash", User.Role.USER)); return email;
    }
    @Test void crudOwnershipValidationAndHistoricalActivity() throws Exception {
        String email = account(), other = account();
        String valid = "{\"name\":\"Dom\",\"latitude\":90,\"longitude\":-180,\"radiusM\":1}";
        mvc.perform(post("/api/places").with(csrf()).contentType("application/json").content(valid)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/places").with(user(email)).with(csrf()).contentType("application/json")
                .content("{\"name\":\" \",\"latitude\":91,\"longitude\":-181,\"radiusM\":0}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.name").exists()).andExpect(jsonPath("$.fields.latitude").exists())
                .andExpect(jsonPath("$.fields.longitude").exists()).andExpect(jsonPath("$.fields.radiusM").exists());
        mvc.perform(post("/api/places").with(user(email)).with(csrf()).contentType("application/json").content(valid)).andExpect(status().isCreated());
        Place place = places.findByUserIdAndDeletedFalseOrderByName(users.findByEmail(email).orElseThrow().getId()).getFirst();
        for (var method : new String[]{"GET", "PUT", "DELETE"}) {
            mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method), "/api/places/" + place.id).with(user(other)).with(csrf())
                    .contentType("application/json").content(valid)).andExpect(status().isNotFound());
        }
        mvc.perform(put("/api/places/" + place.id).with(user(email)).with(csrf()).contentType("application/json").content(valid.replace("90", "-90").replace("-180", "180"))).andExpect(status().isOk());
        String activity = "{\"title\":\"Delo\",\"startAt\":\"2026-11-12T09:00:00Z\",\"endAt\":\"2026-11-12T10:00:00Z\",\"categoryId\":\"00000000-0000-0000-0000-000000000001\",\"placeId\":\"" + place.id + "\"}";
        mvc.perform(post("/api/activities").with(user(other)).with(csrf()).contentType("application/json").content(activity)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/activities").with(user(email)).with(csrf()).contentType("application/json").content(activity)).andExpect(status().isCreated());
        long count = activities.count();
        mvc.perform(delete("/api/places/" + place.id).with(user(email)).with(csrf())).andExpect(status().isNoContent());
        assertThat(activities.count()).isEqualTo(count);
        mvc.perform(get("/api/places/" + place.id).with(user(email))).andExpect(status().isNotFound());
        mvc.perform(post("/api/activities").with(user(email)).with(csrf()).contentType("application/json").content(activity)).andExpect(status().isBadRequest());
    }
}

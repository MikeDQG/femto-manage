package si.um.feri.riris.backendfm.api.place;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import si.um.feri.riris.backendfm.persistence.activity.ActivityRepository;
import si.um.feri.riris.backendfm.persistence.place.Place;
import si.um.feri.riris.backendfm.persistence.place.PlaceRepository;
import si.um.feri.riris.backendfm.persistence.user.User;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class PlaceRecognitionTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PlaceRepository places;
    @Autowired ActivityRepository activities;

    private User account() {
        return users.save(new User(UUID.randomUUID() + "@example.test", "hash", User.Role.USER));
    }

    private Place place(User owner, String name, double latitude, double longitude, int radius, boolean deleted) {
        Place place = new Place();
        place.id = UUID.randomUUID(); place.userId = owner.getId(); place.name = name;
        place.latitude = latitude; place.longitude = longitude; place.radiusM = radius; place.deleted = deleted;
        return places.saveAndFlush(place);
    }

    @Test void nearestTieBoundaryOwnershipAndNoSideEffects() throws Exception {
        User owner = account(), other = account();
        Place near = place(owner, "Blizu", 0, 0, 1000, false);
        Place tied = place(owner, "Enako", 0, 0, 1000, false);
        place(owner, "Dalje", 0, 0.002, 1000, false);
        place(owner, "Izbrisan", 0, 0, 1000, true);
        place(other, "Tuj", 0, 0, 1000, false);
        long placeCount = places.count(), activityCount = activities.count();
        mvc.perform(get("/api/places/recognition?latitude=0&longitude=0").with(user(owner.getEmail())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.recognized").value(true))
                .andExpect(jsonPath("$.candidates.length()").value(2));
        mvc.perform(get("/api/places/recognition?latitude=0&longitude=0.002").with(user(owner.getEmail())))
                .andExpect(jsonPath("$.candidates.length()").value(1))
                .andExpect(jsonPath("$.candidates[0].name").value("Dalje"));
        mvc.perform(get("/api/places/recognition?latitude=0&longitude=1").with(user(owner.getEmail())))
                .andExpect(jsonPath("$.recognized").value(false))
                .andExpect(jsonPath("$.candidates.length()").value(0));
        mvc.perform(get("/api/places/recognition?latitude=0&longitude=0").with(user(other.getEmail())))
                .andExpect(jsonPath("$.candidates.length()").value(1))
                .andExpect(jsonPath("$.candidates[0].name").value("Tuj"));
        mvc.perform(get("/api/places/recognition?latitude=91&longitude=0").with(user(owner.getEmail())))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/places/recognition?latitude=0&longitude=181").with(user(owner.getEmail())))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/places/recognition?latitude=0&longitude=0"))
                .andExpect(status().isUnauthorized());
        assertThat(places.count()).isEqualTo(placeCount);
        assertThat(activities.count()).isEqualTo(activityCount);
        assertThat(near.id).isNotEqualTo(tied.id);
    }

    @Test void exactRadiusIsIncluded() throws Exception {
        User owner = account();
        // On the equator, one degree of longitude spans this haversine distance.
        double distance = 2 * 6_371_000 * Math.asin(Math.sin(Math.toRadians(0.001) / 2));
        place(owner, "Meja", 0, 0.001, (int) Math.ceil(distance), false);
        mvc.perform(get("/api/places/recognition?latitude=0&longitude=0").with(user(owner.getEmail())))
                .andExpect(jsonPath("$.candidates.length()").value(1));
    }
}

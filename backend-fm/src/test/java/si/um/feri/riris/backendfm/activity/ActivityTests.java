package si.um.feri.riris.backendfm.activity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import si.um.feri.riris.backendfm.auth.UserRepository;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActivityTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired ActivityRepository activities;
    @Autowired PlaceRepository places;

    private String account() {
        String email = UUID.randomUUID() + "@example.com";
        users.save(new si.um.feri.riris.backendfm.auth.User(email, "hash", si.um.feri.riris.backendfm.auth.User.Role.USER));
        return email;
    }

    private String payload(String start, String end, boolean confirm, String placeId) {
        return "{\"title\":\"Učenje\",\"startAt\":\"" + start + "\",\"endAt\":\"" + end
                + "\",\"categoryId\":\"00000000-0000-0000-0000-000000000001\",\"placeId\":"
                + placeId + ",\"confirmOverlap\":" + confirm + "}";
    }

    private void create(String email, String body, int status) throws Exception {
        mvc.perform(post("/api/activities").with(user(email)).with(csrf())
                .contentType("application/json").content(body)).andExpect(status().is(status));
    }

    @Test
    void validatesOwnershipAndRequiredFields() throws Exception {
        String owner = account();
        String other = account();
        Place place = new Place();
        place.id = UUID.randomUUID(); place.userId = users.findByEmail(other).orElseThrow().getId();
        place.name = "Tuj kraj"; place.radiusM = 100;
        places.save(place);
        create(owner, "{}", 400);
        create(owner, payload("2026-11-12T10:00:00Z", "2026-11-12T09:00:00Z", false, "null"), 400);
        create(owner, payload("2026-11-12T09:00:00Z", "2026-11-12T10:00:00Z", false, "\"" + place.id + "\""), 400);
        mvc.perform(post("/api/activities").with(csrf()).contentType("application/json")
                .content(payload("2026-11-12T09:00:00Z", "2026-11-12T10:00:00Z", false, "null")))
                .andExpect(status().isUnauthorized());
        create(owner, payload("2026-11-12T09:00:00Z", "2026-11-12T10:00:00Z", false, "null"), 201);
    }

    @Test
    void detectsPartialAndFullOverlapButNotTouchingOrOtherUsers() throws Exception {
        String owner = account();
        String other = account();
        create(owner, payload("2026-11-13T09:00:00Z", "2026-11-13T10:00:00Z", false, "null"), 201);
        create(owner, payload("2026-11-13T09:30:00Z", "2026-11-13T10:30:00Z", false, "null"), 409);
        create(owner, payload("2026-11-13T08:00:00Z", "2026-11-13T11:00:00Z", false, "null"), 409);
        create(owner, payload("2026-11-13T10:00:00Z", "2026-11-13T11:00:00Z", false, "null"), 201);
        create(other, payload("2026-11-13T09:30:00Z", "2026-11-13T10:30:00Z", false, "null"), 201);
        create(owner, payload("2026-11-13T09:30:00Z", "2026-11-13T10:30:00Z", true, "null"), 201);
    }
}

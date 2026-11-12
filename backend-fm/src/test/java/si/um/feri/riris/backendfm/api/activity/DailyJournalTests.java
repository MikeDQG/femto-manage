package si.um.feri.riris.backendfm.api.activity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import si.um.feri.riris.backendfm.persistence.activity.Activity;
import si.um.feri.riris.backendfm.persistence.activity.ActivityRepository;
import si.um.feri.riris.backendfm.persistence.place.Place;
import si.um.feri.riris.backendfm.persistence.place.PlaceRepository;
import si.um.feri.riris.backendfm.persistence.user.User;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class DailyJournalTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired ActivityRepository activities;
    @Autowired PlaceRepository places;

    private User account() {
        return users.saveAndFlush(new User(UUID.randomUUID() + "@example.test", "hash", User.Role.USER));
    }

    private void activity(User owner, String start, String end, UUID category, String status, UUID placeId) {
        Activity activity = new Activity();
        activity.id = UUID.randomUUID(); activity.userId = owner.getId(); activity.categoryId = category;
        activity.placeId = placeId; activity.title = "Preizkus"; activity.startAt = Instant.parse(start);
        activity.endAt = Instant.parse(end); activity.source = "MANUAL"; activity.status = status;
        activity.createdAt = Instant.now(); activity.updatedAt = activity.createdAt;
        activities.saveAndFlush(activity);
    }

    @Test void clipsMidnightCountsOverlapAndKeepsOwnersSeparate() throws Exception {
        User owner = account(), other = account();
        UUID work = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Place place = new Place(); place.id = UUID.randomUUID(); place.userId = owner.getId();
        place.name = "Dom"; place.radiusM = 100; place.deleted = true; places.saveAndFlush(place);
        activity(owner, "2026-11-11T22:30:00Z", "2026-11-11T23:30:00Z", work, "CONFIRMED", place.id);
        activity(owner, "2026-11-11T23:00:00Z", "2026-11-12T00:00:00Z", work, "CONFIRMED", null);
        activity(owner, "2026-11-11T22:30:00Z", "2026-11-11T23:30:00Z", work, "DRAFT", null);
        activity(other, "2026-11-11T22:30:00Z", "2026-11-11T23:30:00Z", work, "CONFIRMED", null);
        long before = activities.count();
        mvc.perform(get("/api/journal/day?date=2026-11-12&zone=Europe/Ljubljana").with(user(owner.getEmail())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.activities.length()").value(2))
                .andExpect(jsonPath("$.activities[0].durationMinutes").value(30))
                .andExpect(jsonPath("$.activities[0].placeName").value("Dom"))
                .andExpect(jsonPath("$.totals[?(@.code == 'WORK')].minutes").value(90))
                .andExpect(jsonPath("$.totals[?(@.code == 'REST')].minutes").value(0));
        mvc.perform(get("/api/journal/day?date=2026-11-12&zone=Europe/Ljubljana").with(user(other.getEmail())))
                .andExpect(jsonPath("$.activities.length()").value(1))
                .andExpect(jsonPath("$.totals[?(@.code == 'WORK')].minutes").value(30));
        mvc.perform(get("/api/journal/day?date=2026-11-13&zone=Europe/Ljubljana").with(user(owner.getEmail())))
                .andExpect(jsonPath("$.activities.length()").value(0))
                .andExpect(jsonPath("$.totals[?(@.code == 'WORK')].minutes").value(0));
        mvc.perform(get("/api/journal/day?date=2026-11-12&zone=Invalid/Zone").with(user(owner.getEmail())))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/journal/day?date=2026-11-12&zone=Europe/Ljubljana"))
                .andExpect(status().isUnauthorized());
        assertThat(activities.count()).isEqualTo(before);
    }

    @Test void localDayUsesDaylightSavingBoundaries() throws Exception {
        User owner = account();
        UUID work = UUID.fromString("00000000-0000-0000-0000-000000000001");
        activity(owner, "2026-03-28T22:30:00Z", "2026-03-29T01:30:00Z", work, "CONFIRMED", null);
        mvc.perform(get("/api/journal/day?date=2026-03-29&zone=Europe/Ljubljana").with(user(owner.getEmail())))
                .andExpect(jsonPath("$.activities.length()").value(1))
                .andExpect(jsonPath("$.totals[?(@.code == 'WORK')].minutes").value(150));
    }
}

package si.um.feri.riris.backendfm.api.activity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import si.um.feri.riris.backendfm.persistence.activity.*;
import si.um.feri.riris.backendfm.persistence.category.*;
import si.um.feri.riris.backendfm.persistence.place.*;
import si.um.feri.riris.backendfm.persistence.user.*;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ActivityUpdateTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired ActivityRepository activities;
    @Autowired CategoryRepository categories;
    @Autowired PlaceRepository places;
    private static final UUID WORK = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MOVEMENT = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private User account() { return users.saveAndFlush(new User(UUID.randomUUID()+"@example.test", "unused", User.Role.USER)); }
    private Activity activity(User owner, String start, String end) {
        Activity activity = new Activity();
        activity.id = UUID.randomUUID(); activity.userId = owner.getId(); activity.categoryId = WORK;
        activity.title = "Izvorni naziv"; activity.description = "Izvorni opis";
        activity.startAt = Instant.parse(start); activity.endAt = Instant.parse(end);
        activity.source = "AI"; activity.status = "CONFIRMED";
        activity.createdAt = Instant.parse("2026-01-01T00:00:00Z"); activity.updatedAt = activity.createdAt;
        return activities.saveAndFlush(activity);
    }
    private String input(String start, String end, UUID category, UUID place, boolean confirm) {
        return """
                {"title":"Popravljena aktivnost","description":"Nov opis","startAt":"%s","endAt":"%s",
                 "categoryId":"%s","placeId":%s,"confirmOverlap":%s}
                """.formatted(start, end, category, place == null ? "null" : "\""+place+"\"", confirm);
    }
    private String input(Activity activity) {
        return input(activity.startAt.toString(), activity.endAt.toString(), activity.categoryId, activity.placeId, false);
    }
    private org.springframework.test.web.servlet.ResultActions update(User user, Activity activity, String body) throws Exception {
        return mvc.perform(put("/api/activities/"+activity.id).with(user(user.getEmail())).with(csrf())
                .contentType("application/json").content(body));
    }

    @Test void updatesSameRecordAndPreservesMetadataAndOriginalMidnightInterval() throws Exception {
        User owner = account();
        Activity original = activity(owner,"2026-11-12T22:30:15Z","2026-11-13T01:30:45Z");
        long before = activities.count();
        mvc.perform(get("/api/activities/"+original.id).with(user(owner.getEmail())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.description").value("Izvorni opis"))
                .andExpect(jsonPath("$.startAt").value(original.startAt.toString()))
                .andExpect(jsonPath("$.endAt").value(original.endAt.toString()));
        update(owner, original, input(original)).andExpect(status().isOk());
        Activity saved = activities.findById(original.id).orElseThrow();
        assertThat(activities.count()).isEqualTo(before);
        assertThat(saved.userId).isEqualTo(owner.getId());
        assertThat(saved.createdAt).isEqualTo(original.createdAt);
        assertThat(saved.updatedAt).isAfter(original.updatedAt);
        assertThat(saved.source).isEqualTo("AI"); assertThat(saved.status).isEqualTo("CONFIRMED");
        assertThat(saved.startAt).isEqualTo(original.startAt); assertThat(saved.endAt).isEqualTo(original.endAt);
        assertThat(saved.title).isEqualTo("Popravljena aktivnost"); assertThat(saved.description).isEqualTo("Nov opis");
    }

    @Test void rejectsInvalidInputAndMetadataWithoutPartialChanges() throws Exception {
        User owner = account(); Activity original = activity(owner,"2026-11-12T09:00:00Z","2026-11-12T10:00:00Z");
        update(owner, original, "{}").andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.title").exists());
        update(owner, original, input("2026-11-12T11:00:00Z","2026-11-12T10:00:00Z",WORK,null,false))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.endAt").exists());
        update(owner, original, input(original).replace("Nov opis", "x".repeat(2001))).andExpect(status().isBadRequest());
        update(owner, original, input(original).replace("Popravljena aktivnost", "x".repeat(201))).andExpect(status().isBadRequest());
        for (String field : new String[]{"id","userId","createdAt","updatedAt","source","status"})
            update(owner, original, input(original).replaceFirst("\\{", "{\""+field+"\":\"changed\","))
                    .andExpect(status().isBadRequest());
        Activity saved = activities.findById(original.id).orElseThrow();
        assertThat(saved.title).isEqualTo(original.title); assertThat(saved.description).isEqualTo(original.description);
        assertThat(saved.updatedAt).isEqualTo(original.updatedAt); assertThat(saved.startAt).isEqualTo(original.startAt);
    }

    @Test void protectsReadsWritesAndDraftsAndRequiresCsrf() throws Exception {
        User owner = account(), other = account();
        Activity original = activity(owner,"2026-11-12T09:00:00Z","2026-11-12T10:00:00Z");
        for (UUID id : new UUID[]{original.id,UUID.randomUUID()}) {
            mvc.perform(get("/api/activities/"+id).with(user(other.getEmail()))).andExpect(status().isNotFound());
            mvc.perform(put("/api/activities/"+id).with(user(other.getEmail())).with(csrf())
                    .contentType("application/json").content(input(original))).andExpect(status().isNotFound());
        }
        mvc.perform(get("/api/activities/"+original.id)).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/activities/"+original.id).with(csrf()).contentType("application/json").content(input(original)))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/activities/"+original.id).with(user(owner.getEmail())).contentType("application/json").content(input(original)))
                .andExpect(status().isForbidden());
        original.status="DRAFT"; activities.saveAndFlush(original);
        update(owner,original,input(original)).andExpect(status().isNotFound());
        mvc.perform(get("/api/activities/"+original.id).with(user(owner.getEmail()))).andExpect(status().isNotFound());
        assertThat(activities.findById(original.id).orElseThrow().title).isEqualTo("Izvorni naziv");
    }

    @Test void excludesSelfButRequiresConfirmationForOtherConfirmedActivitiesAndAllowsTouching() throws Exception {
        User owner = account();
        Activity first = activity(owner,"2026-11-12T09:00:00Z","2026-11-12T10:00:00Z");
        activity(owner,"2026-11-12T11:00:00Z","2026-11-12T12:00:00Z");
        activity(account(),"2026-11-12T09:00:00Z","2026-11-12T10:00:00Z");
        update(owner,first,input(first)).andExpect(status().isOk());
        String body = input("2026-11-12T10:30:00Z","2026-11-12T11:30:00Z",WORK,null,false);
        update(owner,first,body).andExpect(status().isConflict())
                .andExpect(jsonPath("$.overlaps.length()").value(1)).andExpect(jsonPath("$.overlaps[0].title").value("Izvorni naziv"))
                .andExpect(jsonPath("$.overlaps[0].startAt").value("2026-11-12T11:00:00Z"));
        assertThat(activities.findById(first.id).orElseThrow().startAt).isEqualTo(first.startAt);
        update(owner,first,body.replace("false","true")).andExpect(status().isOk());
        update(owner,first,input("2026-11-12T10:00:00Z","2026-11-12T11:00:00Z",WORK,null,false)).andExpect(status().isOk());
    }

    @Test void keepsHistoricalLinksButRejectsNewInactiveOrDeletedOrForeignLinks() throws Exception {
        User owner=account(); Activity original=activity(owner,"2026-11-12T09:00:00Z","2026-11-12T10:00:00Z");
        Category inactive=new Category(); inactive.id=UUID.randomUUID(); inactive.code="OLD-"+inactive.id; inactive.name="Stara"; inactive.active=false;
        categories.saveAndFlush(inactive);
        Place deleted=new Place(); deleted.id=UUID.randomUUID(); deleted.userId=owner.getId(); deleted.name="Stari kraj"; deleted.radiusM=100; deleted.deleted=true;
        places.saveAndFlush(deleted);
        original.categoryId=inactive.id; original.placeId=deleted.id; activities.saveAndFlush(original);
        update(owner,original,input(original)).andExpect(status().isOk());
        mvc.perform(get("/api/activities/"+original.id).with(user(owner.getEmail())))
                .andExpect(jsonPath("$.categoryName").value("Stara")).andExpect(jsonPath("$.placeName").value("Stari kraj"));
        Activity fresh=activity(owner,"2026-11-14T09:00:00Z","2026-11-14T10:00:00Z");
        update(owner,fresh,input(fresh.startAt.toString(),fresh.endAt.toString(),inactive.id,deleted.id,false))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.categoryId").exists()).andExpect(jsonPath("$.fields.placeId").exists());
        deleted.deleted=false; deleted.userId=account().getId(); places.saveAndFlush(deleted);
        update(owner,original,input(original)).andExpect(status().isBadRequest());
        update(owner,original,input(original.startAt.toString(),original.endAt.toString(),WORK,null,false)).andExpect(status().isOk());
    }

    @Test void movesAcrossDaysAndRecalculatesCategoryTotals() throws Exception {
        User owner=account(); Activity original=activity(owner,"2026-11-12T22:30:00Z","2026-11-13T00:30:00Z");
        update(owner,original,input("2026-11-14T09:00:00Z","2026-11-14T10:30:00Z",MOVEMENT,null,false)).andExpect(status().isOk());
        for (String date : new String[]{"2026-11-12","2026-11-13"})
            mvc.perform(get("/api/journal/day").param("date",date).param("zone","Europe/Ljubljana").with(user(owner.getEmail())))
                    .andExpect(jsonPath("$.activities.length()").value(0));
        mvc.perform(get("/api/journal/day").param("date","2026-11-14").param("zone","Europe/Ljubljana").with(user(owner.getEmail())))
                .andExpect(jsonPath("$.activities[0].id").value(original.id.toString()))
                .andExpect(jsonPath("$.totals[?(@.code == 'MOVEMENT')].minutes").value(90));
    }
}

package si.um.feri.riris.backendfm.api.activity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import si.um.feri.riris.backendfm.engine.activity.ActivityTextExtractor;
import si.um.feri.riris.backendfm.persistence.activity.ActivityRepository;
import si.um.feri.riris.backendfm.persistence.category.*;
import si.um.feri.riris.backendfm.persistence.place.*;
import si.um.feri.riris.backendfm.persistence.user.*;
import java.time.*;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class TextActivityDraftTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired CategoryRepository categories;
    @Autowired PlaceRepository places;
    @Autowired ActivityRepository activities;
    @MockitoBean ActivityTextExtractor extractor;
    private static final String INPUT="""
            {"text":"Danes od 9. do 11. ure sem študiral", "referenceDate":"2026-11-12", "zone":"Europe/Ljubljana"}
            """;
    private User account() { return users.saveAndFlush(new User(UUID.randomUUID()+"@test.example","hash",User.Role.USER)); }
    private Place place(User owner, String name) {
        Place p=new Place(); p.id=UUID.randomUUID(); p.userId=owner.getId(); p.name=name; p.radiusM=100;
        return places.saveAndFlush(p);
    }
    private void result(String title,String date,String start,String end,String code,String place) {
        when(extractor.extract(anyString(),any(),any(),anyList())).thenReturn(new ActivityTextExtractor.Extraction(title,date,start,end,code,place));
    }
    @Test void extractsLocalTodayAndActiveStudyWithoutWritingThenConfirmsAiSource() throws Exception {
        User owner=account();
        Category study=new Category(); study.id=UUID.randomUUID(); study.code="STUDY_"+UUID.randomUUID().toString().substring(0,8); study.name="Študij"; study.active=true;
        categories.saveAndFlush(study);
        Place p=place(owner,"Knjižnica");
        result("Študij","2026-11-12","09:00","11:00",study.code,"Knjižnica");
        long before=activities.count();
        mvc.perform(post("/api/activities/text-draft").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content(INPUT))
                .andExpect(status().isOk()).andExpect(jsonPath("$.startAt").value("2026-11-12T08:00:00Z"))
                .andExpect(jsonPath("$.endAt").value("2026-11-12T10:00:00Z")).andExpect(jsonPath("$.categoryId").value(study.id.toString()))
                .andExpect(jsonPath("$.placeId").value(p.id.toString())).andExpect(jsonPath("$.source").value("AI"));
        verify(extractor).extract(eq("Danes od 9. do 11. ure sem študiral"),eq(LocalDate.of(2026,11,12)),eq(ZoneId.of("Europe/Ljubljana")),anyList());
        assertThat(activities.count()).isEqualTo(before);
        mvc.perform(post("/api/activities").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content("""
                {"title":"Študij", "startAt":"2026-11-12T08:00:00Z", "endAt":"2026-11-12T10:00:00Z", "categoryId":"%s", "source":"AI"}
                """.formatted(study.id))).andExpect(status().isCreated()).andExpect(jsonPath("$.source").value("AI"));
    }
    @Test void incompleteInvalidInactiveAndAmbiguousResultsNeedCompletion() throws Exception {
        User owner=account(); place(owner,"Dom"); place(owner,"Dom");
        Category inactive=new Category(); inactive.id=UUID.randomUUID(); inactive.code="INACTIVE_"+UUID.randomUUID().toString().substring(0,8); inactive.name="Neaktivna"; inactive.active=false;
        categories.saveAndFlush(inactive);
        result(null,"2026-02-30","09:00","08:00",inactive.code,"Dom");
        mvc.perform(post("/api/activities/text-draft").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content(INPUT))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fields.title").exists()).andExpect(jsonPath("$.fields.startAt").exists())
                .andExpect(jsonPath("$.fields.endAt").exists()).andExpect(jsonPath("$.fields.categoryId").exists())
                .andExpect(jsonPath("$.placeId").isEmpty()).andExpect(jsonPath("$.placeCandidates.length()").value(2));
        result("Delo","2026-11-12","11:00","09:00","WORK",null);
        mvc.perform(post("/api/activities/text-draft").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content(INPUT))
                .andExpect(jsonPath("$.endAt").isEmpty()).andExpect(jsonPath("$.fields.endAt").exists());
        result("Delo","2026-10-25","02:30","04:00","WORK",null);
        mvc.perform(post("/api/activities/text-draft").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content(INPUT))
                .andExpect(jsonPath("$.startAt").isEmpty());
    }
    @Test void foreignPlaceNotExposedAndUnauthenticatedOrInvalidRequestsFail() throws Exception {
        User owner=account(); place(account(),"Skrivni kraj");
        result("Delo","2026-11-12","09:00","11:00","WORK","Skrivni kraj");
        mvc.perform(post("/api/activities/text-draft").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content(INPUT))
                .andExpect(jsonPath("$.placeId").isEmpty()).andExpect(jsonPath("$.placeCandidates.length()").value(0));
        mvc.perform(post("/api/activities/text-draft").with(csrf()).contentType("application/json").content(INPUT)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/activities/text-draft").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content(INPUT.replace("Europe/Ljubljana","invalid"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/activities/text-draft").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content(INPUT.replace("Danes od 9. do 11. ure sem študiral",""))).andExpect(status().isBadRequest());
    }
}

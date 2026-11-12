package si.um.feri.riris.backendfm.api.summary;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import si.um.feri.riris.backendfm.engine.summary.SummarySharingService;
import si.um.feri.riris.backendfm.persistence.activity.*;
import si.um.feri.riris.backendfm.persistence.summary.*;
import si.um.feri.riris.backendfm.persistence.user.*;
import tools.jackson.databind.json.JsonMapper;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class SummarySharingTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired ActivityRepository activities;
    @Autowired DailySummaryRepository summaries;
    @Autowired AccessGrantRepository grants;
    @Autowired SummarySharingService sharing;
    private final JsonMapper json=JsonMapper.builder().build();
    private User account(User.Role role) { return users.saveAndFlush(new User(UUID.randomUUID()+"@test.example","hash",role)); }
    private Activity activity(User owner) {
        Activity a=new Activity(); a.id=UUID.randomUUID(); a.userId=owner.getId(); a.categoryId=UUID.fromString("00000000-0000-0000-0000-000000000001");
        a.title="Zasebna aktivnost"; a.description="Skrivni opis"; a.startAt=Instant.parse("2026-11-12T08:00:00Z"); a.endAt=a.startAt.plusSeconds(3600);
        a.source="AI"; a.status="CONFIRMED"; a.createdAt=Instant.now(); a.updatedAt=a.createdAt; return activities.saveAndFlush(a);
    }
    private String preview(User owner,User mentor,MockHttpSession session) throws Exception {
        return field(mvc.perform(post("/api/summaries/preview").with(user(owner.getEmail())).with(csrf()).session(session).contentType("application/json")
                .content("{\"date\":\"2026-11-12\",\"zone\":\"Europe/Ljubljana\",\"mentorId\":\""+mentor.getId()+"\",\"comment\":\"Za mentorja\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.comment").value("Za mentorja")).andReturn(),"previewId");
    }
    private ResultActions confirm(User owner,MockHttpSession session,String id) throws Exception {
        return mvc.perform(post("/api/summaries/shares").with(user(owner.getEmail())).with(csrf()).session(session).contentType("application/json")
                .content("{\"previewId\":\""+id+"\",\"confirmed\":true}"));
    }
    private String field(MvcResult result,String field) throws Exception { return json.readTree(result.getResponse().getContentAsString()).path(field).asText(); }

    @Test void explicitShareIsPrivateImmutableVersionedAndRevocable() throws Exception {
        User owner=account(User.Role.USER), mentor=account(User.Role.MENTOR), other=account(User.Role.MENTOR), admin=account(User.Role.ADMIN);
        Activity activity=activity(owner); MockHttpSession session=new MockHttpSession();
        mvc.perform(put("/api/journal/reflection?date=2026-11-12").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content("{\"note\":\"Zasebna refleksija\"}")).andExpect(status().isOk());
        long before=summaries.count(), grantCount=grants.count();
        String preview=preview(owner,mentor,session);
        assertThat(summaries.count()).isEqualTo(before); assertThat(grants.count()).isEqualTo(grantCount);
        var created=confirm(owner,session,preview).andExpect(status().isCreated()).andReturn();
        String id=field(created,"summaryId"), grantId=field(created,"id");
        confirm(owner,session,preview).andExpect(status().isConflict());
        String snapshot=mvc.perform(get("/api/summaries/"+id).with(user(mentor.getEmail()).roles("MENTOR")))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"))
                .andExpect(jsonPath("$.totals[?(@.code == 'WORK')].minutes").value(60)).andReturn().getResponse().getContentAsString();
        assertThat(snapshot).doesNotContain("Zasebna aktivnost","Skrivni opis","Zasebna refleksija","startAt","placeId","note","source");
        assertThat(json.readTree(snapshot).properties().stream().map(Map.Entry::getKey)).containsExactlyInAnyOrder("date","totals","comment");
        for(User unauthorized:List.of(other,admin,account(User.Role.USER))) {
            mvc.perform(get("/api/summaries/"+id).with(user(unauthorized.getEmail()).roles(unauthorized.getRole().name()))).andExpect(status().isNotFound());
            mvc.perform(delete("/api/summaries/shares/"+grantId).with(user(unauthorized.getEmail())).with(csrf())).andExpect(status().isNotFound());
        }
        mvc.perform(put("/api/activities/"+activity.id).with(user(owner.getEmail())).with(csrf()).contentType("application/json")
                .content("""
                        {"title":"Popravljena aktivnost","startAt":"2026-11-12T08:00:00Z","endAt":"2026-11-12T10:00:00Z",
                         "categoryId":"00000000-0000-0000-0000-000000000001","description":"Nov opis"}
                        """)).andExpect(status().isOk());
        mvc.perform(put("/api/journal/reflection?date=2026-11-12").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content("{\"note\":\"Sprememba\"}")).andExpect(status().isOk());
        assertThat(mvc.perform(get("/api/summaries/"+id).with(user(mentor.getEmail()))).andReturn().getResponse().getContentAsString()).isEqualTo(snapshot);
        String newer=preview(owner,mentor,session);
        String newId=field(confirm(owner,session,newer).andExpect(status().isCreated()).andReturn(),"summaryId");
        assertThat(newId).isNotEqualTo(id);
        mvc.perform(get("/api/summaries/"+newId).with(user(mentor.getEmail()))).andExpect(jsonPath("$.totals[?(@.code == 'WORK')].minutes").value(120));
        mvc.perform(delete("/api/summaries/shares/"+grantId).with(user(owner.getEmail())).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(delete("/api/summaries/shares/"+grantId).with(user(owner.getEmail())).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/summaries/"+id).with(user(mentor.getEmail()))).andExpect(status().isNotFound());
        mvc.perform(get("/api/summaries/"+id).with(user(owner.getEmail()))).andExpect(status().isOk());
        mvc.perform(get("/api/summaries/shares").with(user(owner.getEmail()))).andExpect(jsonPath("$[?(@.id == '"+grantId+"')].revokedAt").isNotEmpty());
        assertThat(summaries.count()).isEqualTo(before+2); assertThat(activities.findById(activity.id)).isPresent();
    }
    @Test void staleUnknownUnconfirmedAndForeignPreviewsNeverShare() throws Exception {
        User owner=account(User.Role.USER), mentor=account(User.Role.MENTOR), other=account(User.Role.USER);
        Activity activity=activity(owner); MockHttpSession session=new MockHttpSession();
        String preview=preview(owner,mentor,session); long count=summaries.count();
        mvc.perform(post("/api/summaries/shares").with(user(owner.getEmail())).with(csrf()).session(session).contentType("application/json")
                .content("{\"previewId\":\""+preview+"\",\"confirmed\":false}")).andExpect(status().isBadRequest());
        confirm(other,session,preview).andExpect(status().isConflict());
        confirm(owner,new MockHttpSession(),preview).andExpect(status().isConflict());
        activity.title="Spremenjen naziv brez spremembe trajanja"; activities.saveAndFlush(activity);
        confirm(owner,session,preview).andExpect(status().isConflict());
        assertThat(summaries.count()).isEqualTo(count);
        var pending=sharing.preview(owner.getId(),LocalDate.of(2026,11,12),ZoneId.of("Europe/Ljubljana"),mentor.getId(),null);
        var expired=new SummarySharingService.Pending(pending.id(),pending.owner(),pending.mentorId(),pending.zone(),pending.snapshot(),pending.version(),Instant.EPOCH);
        assertThatThrownBy(() -> sharing.confirm(owner.getId(),expired)).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }
    @Test void onlyExistingMentorsAreRecipientsAndAuthenticationIsRequired() throws Exception {
        User owner=account(User.Role.USER), mentor=account(User.Role.MENTOR);
        String invalid="{\"date\":\"2026-11-12\",\"zone\":\"Europe/Ljubljana\",\"mentorId\":\""+owner.getId()+"\"}";
        mvc.perform(post("/api/summaries/preview").with(user(owner.getEmail())).with(csrf()).contentType("application/json").content(invalid)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/summaries/mentors").with(user(owner.getEmail())))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == '"+mentor.getId()+"')].email").value(mentor.getEmail()))
                .andExpect(jsonPath("$[?(@.id == '"+owner.getId()+"')]").isEmpty());
        mvc.perform(get("/api/summaries/mentors")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/summaries/preview").with(csrf()).contentType("application/json").content(invalid)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/summaries/shares").with(user(owner.getEmail()))).andExpect(jsonPath("$.length()").value(0));
    }
}

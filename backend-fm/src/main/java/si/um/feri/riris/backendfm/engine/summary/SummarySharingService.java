package si.um.feri.riris.backendfm.engine.summary;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import si.um.feri.riris.backendfm.engine.activity.DailyJournalService;
import si.um.feri.riris.backendfm.persistence.summary.*;
import si.um.feri.riris.backendfm.persistence.user.*;
import tools.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

@Service
public class SummarySharingService {
    private final DailySummaryRepository summaries;
    private final AccessGrantRepository grants;
    private final UserRepository users;
    private final DailyJournalService journal;
    private final JsonMapper json=JsonMapper.builder().build();
    public SummarySharingService(DailySummaryRepository summaries,AccessGrantRepository grants,UserRepository users,DailyJournalService journal) {
        this.summaries=summaries; this.grants=grants; this.users=users; this.journal=journal;
    }
    public record Mentor(UUID id,String email) {}
    public record Snapshot(LocalDate date,List<DailyJournalService.CategoryTotal> totals,String comment) {}
    public record Pending(UUID id,UUID owner,UUID mentorId,ZoneId zone,Snapshot snapshot,String version,Instant expiresAt) {}
    public record Share(UUID id,UUID summaryId,LocalDate date,Mentor mentor,Instant createdAt,Instant revokedAt) {}
    public List<Mentor> mentors() { return users.findByRoleOrderByEmail(User.Role.MENTOR).stream().map(u -> new Mentor(u.getId(),u.getEmail())).toList(); }
    private User mentor(UUID id) {
        return users.findById(id).filter(u -> u.getRole()==User.Role.MENTOR)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Izberi obstoječega mentorja."));
    }
    private String version(DailyJournalService.Day day) {
        try {
            var canonical=Map.of("activities",day.activities().stream().sorted(Comparator.comparing(a -> a.id().toString())).toList(),
                    "totals",day.totals().stream().sorted(Comparator.comparing(t -> t.categoryId().toString())).toList());
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.writeValueAsString(canonical).getBytes(StandardCharsets.UTF_8)));
        } catch(Exception exception) { throw new IllegalStateException("Izračun različice povzetka ni uspel.",exception); }
    }
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Pending preview(UUID owner,LocalDate date,ZoneId zone,UUID mentorId,String comment) {
        mentor(mentorId);
        var day=journal.day(owner,date,zone);
        return new Pending(UUID.randomUUID(),owner,mentorId,zone,new Snapshot(date,day.totals(),comment),version(day),Instant.now().plusSeconds(900));
    }
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Share confirm(UUID owner,Pending pending) {
        if (!pending.owner().equals(owner) || pending.expiresAt().isBefore(Instant.now())) throw stale();
        User recipient=mentor(pending.mentorId());
        if (!version(journal.day(owner,pending.snapshot().date(),pending.zone())).equals(pending.version())) throw stale();
        DailySummary summary=new DailySummary(); summary.id=UUID.randomUUID(); summary.userId=owner; summary.date=pending.snapshot().date();
        summary.comment=pending.snapshot().comment(); summary.createdAt=Instant.now(); summary.categoryTotals=json.writeValueAsString(pending.snapshot().totals());
        summaries.saveAndFlush(summary);
        AccessGrant grant=new AccessGrant(); grant.id=UUID.randomUUID(); grant.summaryId=summary.id; grant.mentorId=recipient.getId(); grant.createdAt=summary.createdAt;
        grants.saveAndFlush(grant);
        return share(summary,grant,recipient);
    }
    public ResponseStatusException stale() { return new ResponseStatusException(HttpStatus.CONFLICT,"Podatki ali predogled so se spremenili. Pridobi nov predogled in ponovno potrdi deljenje."); }
    private Share share(DailySummary summary,AccessGrant grant,User mentor) {
        return new Share(grant.id,summary.id,summary.date,new Mentor(mentor.getId(),mentor.getEmail()),grant.createdAt,grant.revokedAt);
    }
    @Transactional(readOnly=true)
    public List<Share> shares(UUID owner) {
        return summaries.findByUserIdOrderByCreatedAtDesc(owner).stream().flatMap(summary -> grants.findBySummaryId(summary.id).stream()
                .map(grant -> share(summary,grant,users.findById(grant.mentorId).orElseThrow()))).toList();
    }
    @Transactional
    public void revoke(UUID owner,UUID grantId) {
        AccessGrant grant=grants.lockById(grantId).orElseThrow(this::notFound);
        DailySummary summary=summaries.findById(grant.summaryId).orElseThrow(this::notFound);
        if(!summary.userId.equals(owner)) throw notFound();
        if(grant.revokedAt==null) { grant.revokedAt=Instant.now(); grants.saveAndFlush(grant); }
    }
    @Transactional(readOnly=true)
    public Snapshot read(UUID reader,UUID summaryId) {
        DailySummary summary=summaries.findById(summaryId).orElseThrow(this::notFound);
        if(!summary.userId.equals(reader)) {
            if(users.findById(reader).orElseThrow().getRole()!=User.Role.MENTOR
                    || !grants.existsBySummaryIdAndMentorIdAndRevokedAtIsNull(summaryId,reader)) throw notFound();
        }
        var type=json.getTypeFactory().constructCollectionType(List.class,DailyJournalService.CategoryTotal.class);
        return new Snapshot(summary.date,json.readValue(summary.categoryTotals,type),summary.comment);
    }
    private ResponseStatusException notFound() { return new ResponseStatusException(HttpStatus.NOT_FOUND,"Povzetek ali dovoljenje ni dostopno."); }
}

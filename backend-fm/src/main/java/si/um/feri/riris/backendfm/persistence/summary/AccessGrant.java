package si.um.feri.riris.backendfm.persistence.summary;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="access_grant")
public class AccessGrant {
    @Id public UUID id;
    @Column(name="summary_id",nullable=false,updatable=false) public UUID summaryId;
    @Column(name="mentor_id",nullable=false,updatable=false) public UUID mentorId;
    @Column(name="created_at",nullable=false,updatable=false) public Instant createdAt;
    @Column(name="revoked_at") public Instant revokedAt;
}

package si.um.feri.riris.backendfm.persistence.summary;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity @Table(name="daily_summary")
public class DailySummary {
    @Id public UUID id;
    @Column(name="user_id",nullable=false,updatable=false) public UUID userId;
    @Column(nullable=false,updatable=false) public LocalDate date;
    @Column(name="category_totals",nullable=false,updatable=false,columnDefinition="text") public String categoryTotals;
    @Column(length=2000,updatable=false) public String comment;
    @Column(name="created_at",nullable=false,updatable=false) public Instant createdAt;
}

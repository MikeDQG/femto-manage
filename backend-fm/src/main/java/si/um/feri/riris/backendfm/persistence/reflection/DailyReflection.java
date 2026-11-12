package si.um.feri.riris.backendfm.persistence.reflection;

import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name="daily_reflection", uniqueConstraints=@UniqueConstraint(columnNames={"user_id","date"}))
public class DailyReflection {
    @Id public UUID id;
    @Column(name="user_id",nullable=false) public UUID userId;
    @Column(nullable=false) public LocalDate date;
    @Column(length=4000) public String note;
    @Column(length=100) public String mood;
    public Integer energy;
    @Column(name="created_at",nullable=false) public Instant createdAt;
    @Column(name="updated_at",nullable=false) public Instant updatedAt;
}

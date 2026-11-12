package si.um.feri.riris.backendfm.activity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
public class Activity {
    @Id public UUID id;
    @Column(name = "user_id") public UUID userId;
    @Column(name = "category_id") public UUID categoryId;
    @Column(name = "place_id") public UUID placeId;
    public String title;
    public String description;
    @Column(name = "start_at") public Instant startAt;
    @Column(name = "end_at") public Instant endAt;
    public String source;
    public String status;
    @Column(name = "created_at") public Instant createdAt;
    @Column(name = "updated_at") public Instant updatedAt;
}

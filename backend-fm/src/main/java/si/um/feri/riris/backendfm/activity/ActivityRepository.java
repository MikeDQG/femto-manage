package si.um.feri.riris.backendfm.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    List<Activity> findByUserIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAt(
            UUID userId, String status, Instant endAt, Instant startAt);
    List<Activity> findByUserIdAndStatusAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
            UUID userId, String status, Instant dayEnd, Instant dayStart);
}

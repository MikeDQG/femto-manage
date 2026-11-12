package si.um.feri.riris.backendfm.persistence.summary;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface DailySummaryRepository extends JpaRepository<DailySummary,UUID> {
    List<DailySummary> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

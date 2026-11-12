package si.um.feri.riris.backendfm.persistence.summary;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface AccessGrantRepository extends JpaRepository<AccessGrant,UUID> {
    List<AccessGrant> findBySummaryId(UUID summaryId);
    boolean existsBySummaryIdAndMentorIdAndRevokedAtIsNull(UUID summaryId,UUID mentorId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from AccessGrant g where g.id = :id")
    Optional<AccessGrant> lockById(@Param("id") UUID id);
}

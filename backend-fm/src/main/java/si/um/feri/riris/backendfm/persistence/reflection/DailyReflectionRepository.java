package si.um.feri.riris.backendfm.persistence.reflection;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;

public interface DailyReflectionRepository extends JpaRepository<DailyReflection, UUID> {
    Optional<DailyReflection> findByUserIdAndDate(UUID userId, LocalDate date);
}

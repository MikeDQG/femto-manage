package si.um.feri.riris.backendfm.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PlaceRepository extends JpaRepository<Place, UUID> {
    List<Place> findByUserIdAndDeletedFalseOrderByName(UUID userId);
    java.util.Optional<Place> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);
}

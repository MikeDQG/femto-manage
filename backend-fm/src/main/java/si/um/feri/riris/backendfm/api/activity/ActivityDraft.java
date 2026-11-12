package si.um.feri.riris.backendfm.api.activity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.List;

public record ActivityDraft(String title, Instant startAt, Instant endAt, UUID categoryId,
                            UUID placeId, String source, Map<String, String> fields, List<PlaceCandidate> placeCandidates) {
    public record PlaceCandidate(UUID id, String name) {}
}

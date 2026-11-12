package si.um.feri.riris.backendfm.api.activity;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ActivityUpdateRequest(String title, OffsetDateTime startAt, OffsetDateTime endAt,
                                    UUID categoryId, UUID placeId, String description, Boolean confirmOverlap) {
    @JsonAnySetter
    public void unknown(String name, Object value) { throw new IllegalArgumentException("Neznano polje: " + name); }

    public ActivityRequest asActivityRequest() {
        return new ActivityRequest(title, startAt, endAt, categoryId, placeId, description, confirmOverlap, null);
    }
}

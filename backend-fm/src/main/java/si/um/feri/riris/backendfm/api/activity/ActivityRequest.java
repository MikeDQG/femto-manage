package si.um.feri.riris.backendfm.api.activity;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActivityRequest(String title, OffsetDateTime startAt, OffsetDateTime endAt,
                              UUID categoryId, UUID placeId, String description,
                              Boolean confirmOverlap) {}

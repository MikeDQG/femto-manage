package si.um.feri.riris.backendfm.api.error;

import java.util.Map;

public record ApiError(String message, Map<String, String> fields) {
    static ApiError message(String message) { return new ApiError(message, Map.of()); }
}

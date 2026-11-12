package si.um.feri.riris.backendfm.api.auth;
import si.um.feri.riris.backendfm.persistence.user.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, User.Role role, Instant createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}

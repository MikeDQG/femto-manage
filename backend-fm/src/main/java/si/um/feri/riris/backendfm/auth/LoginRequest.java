package si.um.feri.riris.backendfm.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import java.nio.charset.StandardCharsets;

public record LoginRequest(
        @NotBlank(message = "Vnesi e-poštni naslov.") @Size(max = 254) String email,
        @NotBlank(message = "Vnesi geslo.") @Size(max = 72) String password) {
    public LoginRequest { email = RegistrationRequest.normalizeEmail(email); }

    @AssertTrue(message = "Geslo lahko vsebuje največ 72 bajtov UTF-8 in ne sme vsebovati ničelnega znaka.")
    public boolean isPasswordEncodingValid() {
        return password == null || (password.indexOf('\0') < 0
                && password.getBytes(StandardCharsets.UTF_8).length <= 72);
    }
}

package si.um.feri.riris.backendfm.auth;

import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public record RegistrationRequest(
        @NotBlank(message = "Vnesi e-poštni naslov.")
        @Email(message = "Vnesi veljaven e-poštni naslov.")
        @Size(max = 254, message = "E-poštni naslov je predolg.") String email,
        @NotBlank(message = "Vnesi geslo.")
        @Size(min = 12, max = 72, message = "Geslo mora imeti od 12 do 72 znakov.") String password,
        @Pattern(regexp = "USER", message = "Registracija omogoča samo vlogo USER.") String role) {

    public RegistrationRequest {
        email = normalizeEmail(email);
    }

    static String normalizeEmail(String email) {
        return email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }

    @AssertTrue(message = "Geslo lahko vsebuje največ 72 bajtov UTF-8 in ne sme vsebovati ničelnega znaka.")
    public boolean isPasswordEncodingValid() {
        return password == null || (password.indexOf('\0') < 0
                && password.getBytes(StandardCharsets.UTF_8).length <= 72);
    }
}

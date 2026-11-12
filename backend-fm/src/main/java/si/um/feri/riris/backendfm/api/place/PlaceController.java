package si.um.feri.riris.backendfm.api.place;
import si.um.feri.riris.backendfm.persistence.place.Place;
import si.um.feri.riris.backendfm.engine.place.PlaceService;
import si.um.feri.riris.backendfm.engine.place.PlaceRecognitionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;
import java.util.*;

@RestController
@RequestMapping("/api/places")
public class PlaceController {
    public record Input(
            @NotBlank(message = "Ime je obvezno.") @Size(max = 120, message = "Ime ima največ 120 znakov.") String name,
            @NotNull(message = "Širina je obvezna.") @DecimalMin(value = "-90", message = "Širina mora biti med −90 in 90.") @DecimalMax(value = "90", message = "Širina mora biti med −90 in 90.") Double latitude,
            @NotNull(message = "Dolžina je obvezna.") @DecimalMin(value = "-180", message = "Dolžina mora biti med −180 in 180.") @DecimalMax(value = "180", message = "Dolžina mora biti med −180 in 180.") Double longitude,
            @NotNull(message = "Radij je obvezen.") @Positive(message = "Radij mora biti pozitiven.") Integer radiusM) {}
    private final PlaceService service;
    private final UserRepository users;
    private final PlaceRecognitionService recognition;
    public PlaceController(PlaceService service, UserRepository users, PlaceRecognitionService recognition) {
        this.service = service; this.users = users; this.recognition = recognition;
    }
    private UUID owner(Authentication auth) { return users.findByEmail(auth.getName()).orElseThrow().getId(); }
    @GetMapping public List<Place> list(Authentication auth) { return service.list(owner(auth)); }
    @GetMapping("/recognition")
    public PlaceRecognitionService.Result recognize(
            @RequestParam double latitude,
            @RequestParam double longitude,
            Authentication auth) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
                || !Double.isFinite(longitude) || longitude < -180 || longitude > 180)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Neveljavne koordinate.");
        return recognition.recognize(owner(auth), latitude, longitude);
    }
    @GetMapping("/{id}") public Place get(@PathVariable UUID id, Authentication auth) { return service.get(id, owner(auth)); }
    @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public Place create(@Valid @RequestBody Input input, Authentication auth) { return service.save(null, owner(auth), input); }
    @PutMapping("/{id}") public Place update(@PathVariable UUID id, @Valid @RequestBody Input input, Authentication auth) { return service.save(id, owner(auth), input); }
    @DeleteMapping("/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, Authentication auth) { service.delete(id, owner(auth)); }
}

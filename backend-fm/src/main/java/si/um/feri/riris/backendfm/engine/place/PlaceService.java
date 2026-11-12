package si.um.feri.riris.backendfm.engine.place;
import si.um.feri.riris.backendfm.persistence.place.PlaceRepository;
import si.um.feri.riris.backendfm.persistence.place.Place;
import si.um.feri.riris.backendfm.api.place.PlaceController;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;

@Service
public class PlaceService {
    private final PlaceRepository places;
    public PlaceService(PlaceRepository places) { this.places = places; }
    public List<Place> list(UUID owner) { return places.findByUserIdAndDeletedFalseOrderByName(owner); }
    public Place get(UUID id, UUID owner) {
        return places.findByIdAndUserIdAndDeletedFalse(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kraj ne obstaja."));
    }
    @Transactional
    public Place save(UUID id, UUID owner, PlaceController.Input input) {
        Place place = id == null ? new Place() : get(id, owner);
        if (id == null) { place.id = UUID.randomUUID(); place.userId = owner; }
        place.name = input.name().trim();
        place.latitude = input.latitude(); place.longitude = input.longitude(); place.radiusM = input.radiusM();
        return places.saveAndFlush(place);
    }
    @Transactional
    public void delete(UUID id, UUID owner) {
        Place place = get(id, owner); place.deleted = true; places.save(place);
    }
}

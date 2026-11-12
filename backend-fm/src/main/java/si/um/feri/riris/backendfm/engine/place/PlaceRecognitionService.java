package si.um.feri.riris.backendfm.engine.place;

import org.springframework.stereotype.Service;
import si.um.feri.riris.backendfm.persistence.place.Place;
import si.um.feri.riris.backendfm.persistence.place.PlaceRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class PlaceRecognitionService {
    private static final double EARTH_RADIUS_M = 6_371_000;
    private static final double TIE_TOLERANCE_M = 0.001;
    private final PlaceRepository places;

    public PlaceRecognitionService(PlaceRepository places) { this.places = places; }

    public record Candidate(UUID id, String name, double distanceM, int radiusM) {}
    public record Result(boolean recognized, List<Candidate> candidates) {}

    public Result recognize(UUID owner, double latitude, double longitude) {
        List<Candidate> matching = places.findByUserIdAndDeletedFalseOrderByName(owner).stream()
                .map(place -> candidate(place, latitude, longitude))
                .filter(candidate -> candidate.distanceM() <= candidate.radiusM())
                .sorted(Comparator.comparingDouble(Candidate::distanceM).thenComparing(Candidate::name))
                .toList();
        if (matching.isEmpty()) return new Result(false, List.of());
        double nearest = matching.getFirst().distanceM();
        return new Result(true, matching.stream()
                .filter(candidate -> candidate.distanceM() - nearest <= TIE_TOLERANCE_M).toList());
    }

    private Candidate candidate(Place place, double latitude, double longitude) {
        double latDelta = Math.toRadians(place.latitude - latitude);
        double lonDelta = Math.toRadians(place.longitude - longitude);
        double a = Math.pow(Math.sin(latDelta / 2), 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(place.latitude))
                * Math.pow(Math.sin(lonDelta / 2), 2);
        double distance = 2 * EARTH_RADIUS_M * Math.asin(Math.min(1, Math.sqrt(a)));
        return new Candidate(place.id, place.name, distance, place.radiusM);
    }
}

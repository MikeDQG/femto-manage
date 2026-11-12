package si.um.feri.riris.backendfm.engine.activity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public interface ActivityTextExtractor {
    record CategoryChoice(String code, String name) {}
    record Extraction(String title, String date, String startTime, String endTime, String categoryCode, String placeName) {}
    Extraction extract(String text, LocalDate referenceDate, ZoneId zone, List<CategoryChoice> categories);
}

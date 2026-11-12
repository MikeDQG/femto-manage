package si.um.feri.riris.backendfm.api.reflection;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ReflectionRequest(@Size(max=4000,message="Opomba ima lahko največ 4000 znakov.") String note,
                                @Size(max=100,message="Počutje ima lahko največ 100 znakov.") String mood,
                                @DecimalMin(value="1",message="Energija mora biti celo število od 1 do 5.")
                                @DecimalMax(value="5",message="Energija mora biti celo število od 1 do 5.")
                                @Digits(integer=1,fraction=0,message="Energija mora biti celo število od 1 do 5.") BigDecimal energy) {
    @JsonAnySetter
    public void unknown(String name, Object value) { throw new IllegalArgumentException("Neznano polje: " + name); }
}

package si.um.feri.riris.backendfm.activity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Place {
    @Id public UUID id;
    @Column(name = "user_id") public UUID userId;
    public String name;
    public double latitude;
    public double longitude;
    @Column(name = "radius_m") public int radiusM;
}

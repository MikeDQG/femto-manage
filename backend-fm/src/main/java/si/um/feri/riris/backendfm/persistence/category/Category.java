package si.um.feri.riris.backendfm.persistence.category;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Category {
    @Id public UUID id;
    public String code;
    public String name;
    public boolean active;
}

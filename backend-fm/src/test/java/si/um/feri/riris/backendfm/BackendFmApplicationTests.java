package si.um.feri.riris.backendfm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class BackendFmApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void applicationStartsAndDatabaseResponds() {
        Integer result = jdbcTemplate.queryForObject("select 1", Integer.class);

        assertThat(result).isEqualTo(1);
    }

}

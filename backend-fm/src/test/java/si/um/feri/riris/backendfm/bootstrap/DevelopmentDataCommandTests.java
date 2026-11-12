package si.um.feri.riris.backendfm.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.riris.backendfm.persistence.user.User;
import si.um.feri.riris.backendfm.persistence.user.UserRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DevelopmentDataCommandTests {
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired org.springframework.context.ApplicationContext context;

    private DevelopmentDataCommand command(String email, String action) {
        return new DevelopmentDataCommand(jdbc, new MockEnvironment()
                .withProperty("SEED_CONFIRM_DEVELOPMENT_DATABASE", "true")
                .withProperty("SEED_USER_EMAIL", email)
                .withProperty("SEED_ACTION", action)
                .withProperty("SEED_REFERENCE_DATE", "2026-11-12"), context);
    }

    private int count(String table, UUID owner) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE user_id = ?", Integer.class, owner);
    }

    @Test
    void createsOnlyForTargetAndDoesNotOverwriteEdits() throws Exception {
        User target = users.saveAndFlush(new User("seed-target@example.test", "unused", User.Role.USER));
        User other = users.saveAndFlush(new User("seed-other@example.test", "unused", User.Role.USER));
        var command = command(target.getEmail(), "insert");
        command.run();
        assertEquals(3, count("place", target.getId()));
        assertEquals(9, count("activity", target.getId()));
        assertEquals(0, count("place", other.getId()));
        assertEquals(0, count("activity", other.getId()));
        assertEquals(6, jdbc.queryForObject("SELECT COUNT(DISTINCT category_id) FROM activity WHERE user_id = ?", Integer.class, target.getId()));
        assertEquals(3, jdbc.queryForObject("SELECT COUNT(DISTINCT CAST(start_at AS DATE)) FROM activity WHERE user_id = ?", Integer.class, target.getId()));
        assertEquals(9, jdbc.queryForObject("SELECT COUNT(*) FROM activity WHERE user_id = ? AND source = 'MANUAL' AND status = 'CONFIRMED' AND end_at > start_at", Integer.class, target.getId()));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM activity a JOIN place p ON p.id = a.place_id WHERE a.user_id = ? AND p.user_id <> a.user_id", Integer.class, target.getId()));
        assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM activity a JOIN activity b ON a.id <> b.id AND a.user_id = b.user_id " +
                "AND a.start_at < b.end_at AND a.end_at > b.start_at WHERE a.user_id = ?", Integer.class, target.getId()) > 0);
        assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM activity WHERE user_id = ? AND CAST(start_at AS DATE) <> CAST(end_at AS DATE)", Integer.class, target.getId()) > 0);
        UUID changed = jdbc.queryForObject("SELECT id FROM activity WHERE user_id = ? ORDER BY title LIMIT 1", UUID.class, target.getId());
        jdbc.update("UPDATE activity SET title = ? WHERE id = ?", "Ročni popravek", changed);
        command.run();
        assertEquals(3, count("place", target.getId()));
        assertEquals(9, count("activity", target.getId()));
        assertEquals("Ročni popravek", jdbc.queryForObject("SELECT title FROM activity WHERE id = ?", String.class, changed));
        command(target.getEmail(), "remove").run();
        assertEquals(0, count("activity", target.getId()));
        assertEquals(0, count("place", target.getId()));
    }

    @Test
    void missingAccountMakesNoChanges() {
        int before = jdbc.queryForObject("SELECT COUNT(*) FROM activity", Integer.class);
        assertThrows(IllegalStateException.class, () -> command("missing@example.test", "insert").run());
        assertEquals(before, jdbc.queryForObject("SELECT COUNT(*) FROM activity", Integer.class));
    }
}

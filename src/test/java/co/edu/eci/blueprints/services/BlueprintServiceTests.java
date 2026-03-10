package co.edu.eci.blueprints.services;

import co.edu.eci.blueprints.filters.IdentityFilter;
import co.edu.eci.blueprints.model.Blueprint;
import co.edu.eci.blueprints.model.Point;
import co.edu.eci.blueprints.persistence.BlueprintNotFoundException;
import co.edu.eci.blueprints.persistence.BlueprintPersistenceException;
import co.edu.eci.blueprints.persistence.InMemoryBlueprintPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BlueprintServiceTests {

    private BlueprintsServices services;

    @BeforeEach
    void setUp() {
        services = new BlueprintsServices(new InMemoryBlueprintPersistence(), new IdentityFilter());
    }

    @Test
    void getAllBlueprints_returnsSeedData() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        assertNotNull(blueprints);
        assertEquals(3, blueprints.size());
    }

    @Test
    void getBlueprintsByAuthor_returnsCorrectSet() throws BlueprintNotFoundException {
        Set<Blueprint> blueprints = services.getBlueprintsByAuthor("john");
        assertEquals(2, blueprints.size());
        assertTrue(blueprints.stream().allMatch(bp -> bp.getAuthor().equals("john")));
    }

    @Test
    void getBlueprintsByAuthor_throwsWhenNotFound() {
        assertThrows(BlueprintNotFoundException.class,
                () -> services.getBlueprintsByAuthor("nobody"));
    }

    @Test
    void getBlueprint_returnsBlueprintByAuthorAndName() throws BlueprintNotFoundException {
        Blueprint bp = services.getBlueprint("john", "house");
        assertNotNull(bp);
        assertEquals("john", bp.getAuthor());
        assertEquals("house", bp.getName());
        assertEquals(4, bp.getPoints().size());
    }

    @Test
    void getBlueprint_throwsWhenNotFound() {
        assertThrows(BlueprintNotFoundException.class,
                () -> services.getBlueprint("john", "missing"));
    }

    @Test
    void addNewBlueprint_persistsAndRetrieves() throws BlueprintPersistenceException, BlueprintNotFoundException {
        Blueprint newBp = new Blueprint("alice", "sketch", List.of(new Point(1, 1), new Point(2, 2)));
        services.addNewBlueprint(newBp);
        Blueprint retrieved = services.getBlueprint("alice", "sketch");
        assertEquals("alice", retrieved.getAuthor());
        assertEquals("sketch", retrieved.getName());
    }

    @Test
    void addNewBlueprint_throwsOnDuplicate() {
        Blueprint dup = new Blueprint("john", "house", List.of(new Point(0, 0)));
        assertThrows(BlueprintPersistenceException.class, () -> services.addNewBlueprint(dup));
    }

    @Test
    void addPoint_appendsToExistingBlueprint() throws BlueprintNotFoundException {
        services.addPoint("john", "house", 99, 99);
        Blueprint bp = services.getBlueprint("john", "house");
        assertEquals(5, bp.getPoints().size());
        Point last = bp.getPoints().get(4);
        assertEquals(99, last.x());
        assertEquals(99, last.y());
    }

    @Test
    void addPoint_throwsWhenBlueprintNotFound() {
        assertThrows(BlueprintNotFoundException.class,
                () -> services.addPoint("nobody", "blueprint", 1, 1));
    }
}

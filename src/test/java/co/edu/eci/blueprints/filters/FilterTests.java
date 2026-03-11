package co.edu.eci.blueprints.filters;

import co.edu.eci.blueprints.model.Blueprint;
import co.edu.eci.blueprints.model.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterTests {

    @Test
    void identityFilter_returnsUnchangedBlueprint() {
        Blueprint bp = new Blueprint("a", "b", List.of(new Point(1, 1), new Point(2, 2)));
        Blueprint result = new IdentityFilter().apply(bp);
        assertSame(bp, result);
        assertEquals(2, result.getPoints().size());
    }

    @Test
    void redundancyFilter_removesConsecutiveDuplicates() {
        Blueprint bp = new Blueprint("a", "b",
                List.of(new Point(1, 1), new Point(1, 1), new Point(2, 2), new Point(2, 2), new Point(3, 3)));
        Blueprint result = new RedundancyFilter().apply(bp);
        assertEquals(3, result.getPoints().size());
        assertEquals(new Point(1, 1), result.getPoints().get(0));
        assertEquals(new Point(2, 2), result.getPoints().get(1));
        assertEquals(new Point(3, 3), result.getPoints().get(2));
    }

    @Test
    void redundancyFilter_noDuplicates_unchanged() {
        Blueprint bp = new Blueprint("a", "b",
                List.of(new Point(1, 1), new Point(2, 2), new Point(3, 3)));
        Blueprint result = new RedundancyFilter().apply(bp);
        assertEquals(3, result.getPoints().size());
    }

    @Test
    void undersamplingFilter_keepsEvenIndexedPoints() {
        Blueprint bp = new Blueprint("a", "b",
                List.of(new Point(0, 0), new Point(1, 1), new Point(2, 2), new Point(3, 3), new Point(4, 4)));
        Blueprint result = new UndersamplingFilter().apply(bp);
        // Even indices: 0, 2, 4 → 3 points
        assertEquals(3, result.getPoints().size());
        assertEquals(new Point(0, 0), result.getPoints().get(0));
        assertEquals(new Point(2, 2), result.getPoints().get(1));
        assertEquals(new Point(4, 4), result.getPoints().get(2));
    }

    @Test
    void undersamplingFilter_twoOrFewerPoints_returnsUnchanged() {
        Blueprint bp = new Blueprint("a", "b", List.of(new Point(0, 0), new Point(1, 1)));
        Blueprint result = new UndersamplingFilter().apply(bp);
        assertSame(bp, result);
    }
}

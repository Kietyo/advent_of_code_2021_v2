import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class IntersectTest {

    @Test
    fun intersect1() {
        Assertions.assertEquals(
            7..8,
            intersectOrNull(0..8, 7..12)
        )
        Assertions.assertEquals(
            7..8,
            intersectOrNull(7..12, 0..8)
        )

        Assertions.assertEquals(
            8..8,
            intersectOrNull(0..8, 8..12)
        )
        Assertions.assertEquals(
            null,
            intersectOrNull(0..8, 9..12)
        )

        Assertions.assertEquals(
            1..7,
            intersectOrNull(0..8, 1..7)
        )
    }
}

import org.junit.Test
import properties.primitive.PArray

internal class FailsTest {
    @Test
    fun pArrayTest() {
        val p = PArray(mutableListOf(), null)
        // assertThrows<PositionalException> { p["", Token()] }
        // assertThrows<PositionalException> { p[0, Token()] }
    }

    @Test
    fun symbolTableTest() {
    }
}

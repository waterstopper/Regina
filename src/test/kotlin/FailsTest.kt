import lexer.PositionalException
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import properties.primitive.PArray
import token.Token

internal class FailsTest {
    @Test
    fun pArrayTest() {
        val p = PArray(mutableListOf(), null)
        assertThrows<PositionalException> { p["", Token()] }
        assertThrows<PositionalException> { p[0, Token()] }
    }

    @Test
    fun symbolTableTest() {

    }
}
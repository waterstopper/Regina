package lexer

import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class LexerTest {
    @Test
    fun failUnterminatedString() {
        val thrown = assertFails {
            Lexer(
                """
                "unterminated
                "
            """
            )
        }
        assertTrue(thrown.message!!.contains("Unterminated string"))
        val thrown2 = assertFails {
            Lexer("\"unterminated")
        }
        assertTrue(thrown2.message!!.contains("Unterminated string"))
    }

    @Test
    fun invalidOperator() {
        val thrown = assertFails { Lexer("a = #") }
        assertTrue(thrown.message!!.contains("Invalid operator"))
    }

    @Test
    fun failNewLine() {
        val thrown = assertFails { Lexer("a = \\ 0") }
        assertTrue(thrown.message!!.contains("Expected new line after \\"))
    }

    @Test
    fun invalidCharacter() {
        val thrown = assertFails { Lexer("a = `") }
        assertTrue(thrown.message!!.contains("Invalid character"))
    }

    @Test
    fun commentOnLineWithStatement(){
        Parser("""a = b // comment
           t = q
        """).statements()
        Parser("""a = b /* comment
           comment continues */ statementStarts()
           thirdOne()
        """).statements()
    }
}

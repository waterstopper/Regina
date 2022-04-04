package lexer

import deprecated.Symbol
import org.junit.Assert
import org.junit.Test
@Deprecated("")
internal class LexerTest {
    private val regex = Regex("([A-Za-z][\\w]*)(.[A-Za-z][\\w]*)*")

    @Test
    fun regexTest() {
        Assert.assertTrue("w".matches(regex))
        Assert.assertTrue("w12.d".matches(regex))
        Assert.assertFalse("w12.12f".matches(regex))

        Assert.assertEquals(Symbol.getToken("w"), Symbol.REF)
    }

    @Test
    fun tokenizeTest() {
//        Assert.assertEquals(
//            OldLexer().tokenize("abd.few24/43+sin(were.w-43)"),
//            listOf(
//                Token(Symbol.REF, "abd.few24", 10),
//                Token(Symbol.DIV, "/", 11),
//                Token(Symbol.NUM, "43", 13),
//                Token(Symbol.ADD, "+", 14),
//                Token(Symbol.FUN, "sin(", 18),
//                Token(Symbol.REF, "were.w", 24),
//                Token(Symbol.SUB, "-", 25),
//                Token(Symbol.NUM, "43", 27),
//                Token(Symbol.RIGHT_PAR, ")", 28)
//            )
//        )
    }
//
//        val s = "abd.few24/43+sin(were.w-43)"
//        val d = "abd.few24/43+sin(were.w-43"
//        println(Lexer().tokenize(s).toStr())
//
//    }
}
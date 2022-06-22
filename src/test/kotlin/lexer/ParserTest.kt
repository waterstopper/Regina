package lexer

import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ParserTest {
    @Test
    fun failCallParsing() {
        val thrown = assertFails { Parser("a = ()(a,b,c)").statements() }
        assertTrue(thrown.message!!.contains("is not invokable"))

        val thrown2 = assertFails { Parser("fun main() {a = +()}").statements() }
        assertTrue(thrown2.message!!.contains("Expected variable or prefix operator"))
    }

    @Test
    fun failInfixOperator() {
//        val thrown = assertFails { Parser("a true").statements() }
//        print(thrown.message)
//        assertTrue(thrown.message!!.contains("Expected infix or suffix operator"))
    }

    @Test
    fun failTwoStatements() {
        val thrownArr = mutableListOf(
            assertFails { Parser("fun main() {a = b b = c}").statements() },
            assertFails { Parser("a() b()").statements() },
            assertFails { Parser("a = b b()").statements() }
        )
        for (exception in thrownArr)
            assertTrue(exception.message!!.contains("Expected block end or line break"))
    }

    @Test
    fun failDictionaryInstance() {
        val thrown = assertFails { Parser("fun main() {a = {a:b, a}}").statements() }
        assertTrue(thrown.message!!.contains("Expected key and value"))
    }

    @Test
    fun failImportWithAs() {
        val thrownArr = mutableListOf(
            assertFails { Parser("import a.b.c as a.b").statements() },
            assertFails { Parser("import a.b.c as a+b").statements() }
        )
        for (exception in thrownArr)
            assertTrue(exception.message!!.contains("Expected non-link identifier after `as`"))
    }

    @Test
    fun importWithoutAs() {
        val thrownArr = mutableListOf(
            assertFails { Parser("import a.b.c").statements() },
            assertFails { Parser("import a+b").statements() }
        )
        for (exception in thrownArr)
            assertTrue(exception.message!!.contains("Imports containing folders in name should be declared"))
    }

    @Test
    fun importIncorrectFolderPath() {
        val thrownArr = mutableListOf(
            assertFails { Parser("import a.b.c").statements() },
            assertFails { Parser("import a+b").statements() }
        )
        for (exception in thrownArr)
            assertTrue(exception.message!!.contains("Imports containing folders in name should be declared"))
    }

    @Test
    fun fail() {
        val thrown = assertFails { Parser("").statements() }
        assertTrue(thrown.message!!.contains(""))
    }

    @Test
    fun failB() {
        val thrown = assertFails { Parser("").statements() }
        assertTrue(thrown.message!!.contains(""))
    }

    @Test
    fun failLinkableToken() {
        val thrown = assertFails { Parser("fun main() {a = 1.[3]}").statements() }
        assertTrue(thrown.message!!.contains("Expected Identifier or Invocation or Index, but got TokenArray"))
    }
}

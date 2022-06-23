package lexer

import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ParserTest {
    @Test
    fun failCallParsing() {
        val thrown = assertFails { Parser("a = [](a,b,c)").statements() }
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
        val thrownArr = listOf(
            assertFails { Parser("fun main() {a = b b = c}").statements() },
            assertFails { Parser("a() b()").statements() },
            assertFails { Parser("a = b b()").statements() }
        )
        for (exception in thrownArr)
            assertTrue(exception.message!!.contains("Expected statement separator"))
    }

    @Test
    fun parseStatementSeparators() {
        Parser("""
            a = b /* comment */ otherStatement(); other()
            last()
        """).statements()
    }

    @Test
    fun failDictionaryInstance() {
        val thrown = assertFails { Parser("fun main() {a = {a:b, a}}").statements() }
        assertTrue(thrown.message!!.contains("Expected key and value"))
    }

    @Test
    fun failImportWithAs() {
        val thrownArr = listOf(
            assertFails { Parser("import a.b.c as a.b").statements() },
            assertFails { Parser("import a.b.c as a+b").statements() }
        )
        for (exception in thrownArr)
            assertTrue(exception.message!!.contains("Expected non-link identifier after `as`"))
    }

    @Test
    fun importWithoutAs() {
        val thrownArr = listOf(
            assertFails { Parser("import a.b.c").statements() },
            assertFails { Parser("import a+b").statements() }
        )
        for (exception in thrownArr)
            assertTrue(exception.message!!.contains("Imports containing folders in name should be declared"))
    }

    @Test
    fun importWithIncorrectFolderPath() {
        val thrown = assertFails { Parser("import a.b().c as t").statements() }
        assertTrue(thrown.message!!.contains("Each folder should be represented as identifier"))

        val nonLinkThrown = assertFails { Parser("import a+b as b").statements() }
        assertTrue(nonLinkThrown.message!!.contains("Expected link or identifier before `as` directive"))

    }

    fun fail() {
        val thrown = assertFails { Parser("").statements() }
        assertTrue(thrown.message!!.contains(""))
    }

    @Test
    fun failLinkableToken() {
        val thrown = assertFails { Parser("fun main() {a = 1.[3]}").statements() }
        assertTrue(thrown.message!!.contains("Expected Identifier or Invocation or Index, but got TokenArray"))
    }
}

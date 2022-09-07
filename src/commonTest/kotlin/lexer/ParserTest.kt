package lexer

import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ParserTest {
    @Test
    fun failCallParsing() {
        val thrown = assertFails { Parser("a = [](a,b,c)", "@NoFile").statements() }
        assertTrue(thrown.message!!.contains("is not invokable"))

        val thrown2 = assertFails { Parser("fun main() {a = +()}", "@NoFile").statements() }
        assertTrue(thrown2.message!!.contains("Expected variable or prefix operator"))
    }

    @Test
    fun failNoIndexInIndexing() {
        val thrown = assertFails { Parser("a[]", "@NoFile").statements() }
        assertTrue(thrown.message!!.contains("Expected index"))
    }

    @Test
    fun failInfixOperator() {
        val thrown = assertFails { Parser("a true", "@NoFile").statements() }
        print(thrown.message)
        assertTrue(thrown.message!!.contains("Expected separator"))
    }

    @Test
    fun failTwoStatements() {
        val thrownArr = listOf(
            assertFails { Parser("fun main() {a = b b = c}", "@NoFile").statements() },
            assertFails { Parser("a() b()", "@NoFile").statements() },
            assertFails { Parser("a = b b()", "@NoFile").statements() }
        )
        for (exception in thrownArr)
            assertTrue(exception.message!!.contains("Expected separator"))
    }

    @Test
    fun parseStatementSeparators() {
        Parser(
            """
            a = b /* comment */ otherStatement(); other()
            last()
        """, "@NoFile"
        ).statements()
    }

    @Test
    fun failDictionaryInstance() {
        val thrown = assertFails { Parser("fun main() {a = {a:b, a}}", "@NoFile").statements() }
        assertTrue(thrown.message!!.contains("Expected key and value"))
    }

    @Test
    fun failImportWithAs() {
        val thrownArr = listOf(
            assertFails { Parser("import a.b.c as a.b", "@NoFile").statements() },
            assertFails { Parser("import a.b.c as a+b", "@NoFile").statements() }
        )
        for (exception in thrownArr)
            assertTrue(exception.message!!.contains("Expected non-link identifier after `as`"))
    }

    @Test
    fun importWithoutAs() {
        val thrownArr = listOf(
            assertFails { Parser("import a.b.c", "@NoFile").statements() },
            assertFails { Parser("import a+b", "@NoFile").statements() }
        )
        for (exception in thrownArr)
            assertTrue(exception.message!!.contains("Imports containing folders in name should be declared"))
    }

    @Test
    fun importWithIncorrectFolderPath() {
        val thrown = assertFails { Parser("import a.b().c as t", "@NoFile").statements() }
        assertTrue(thrown.message!!.contains("Each folder should be represented as identifier"))

        val nonLinkThrown = assertFails { Parser("import a+b as b", "@NoFile").statements() }
        assertTrue(nonLinkThrown.message!!.contains("Expected link or identifier before `as` directive"))

    }

    @Test
    fun failLinkableToken() {
        val thrown = assertFails { Parser("fun main() {a = 1.[3]}", "@NoFile").statements() }
        assertTrue(thrown.message!!.contains("Expected Identifier or Invocation or Index"))

        val thrown2 = assertFails { Parser("fun main() {a = 1.[1,2][3]}", "@NoFile").statements() }
        assertTrue(thrown2.message!!.contains("Expected Identifier or Invocation or Index"))
    }

    @Test
    fun failParenthesesParsing() {
        val thrownEmptyParentheses = assertFails { Parser("a= ()", "@NoFile").statements() }
        assertTrue(thrownEmptyParentheses.message!!.contains("Empty parentheses"))

        val thrownTuple = assertFails { Parser("a= (1,2)", "@NoFile").statements() }
        assertTrue(thrownTuple.message!!.contains("Tuples are not implemented"))
    }

    @Test
    fun ignoreSeparators() {
        eval(
            """
            fun /**/ main/**/() {
                a = 0; a = 0;
                if(a) {} /**/ else {}; if(a) {} else {}
            }
        """
        )
    }

    @Test
    fun controversialSeparatorsTest() {
        eval(
            """
           fun main;(;); {
            if(a()) b = 0; else {}
            test(b == 0)
           }; fun a() {return 1} 
        """
        )
    }
}

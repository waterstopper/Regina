package lexer

import evaluation.Evaluation.eval
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
    fun failNoIndexInIndexing(){
        val thrown = assertFails { Parser("a[]").statements() }
        assertTrue(thrown.message!!.contains("Expected index"))
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
            assertTrue(exception.message!!.contains("Expected separator"))
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

    @Test
    fun failLinkableToken() {
        val thrown = assertFails { Parser("fun main() {a = 1.[3]}").statements() }
        assertTrue(thrown.message!!.contains("Expected Identifier or Invocation or Index, but got"))

        val thrown2 =  assertFails { Parser("fun main() {a = 1.[1,2][3]}").statements() }
        assertTrue(thrown2.message!!.contains("Expected Identifier or Invocation or Index, but got"))
    }

    @Test
    fun failParenthesesParsing() {
        val thrownEmptyParentheses = assertFails { Parser("a= ()").statements() }
        assertTrue(thrownEmptyParentheses.message!!.contains("Empty parentheses"))

        val thrownTuple = assertFails { Parser("a= (1,2)").statements() }
        assertTrue(thrownTuple.message!!.contains("Tuples are not implemented"))
    }

    @Test
    fun ignoreSeparators() {
        eval("""
            fun /**/ main/**/() {
                a = 0; a = 0;
                if(a) {} /**/ else {}; if(a) {} else {}
            }
        """)
    }

    @Test
    fun controversialSeparatorsTest() {
        eval("""
           fun main;(;); {
            if(a()) b = 0; else {}
            test(b == 0)
           }; fun a() {return 1} 
        """)
    }
}

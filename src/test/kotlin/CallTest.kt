import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class CallTest {
    @Test
    fun testSignature() {
        eval(
            """
           fun main() {
                test(s() == 0)
                test(s(1) == 1)
                test(s(a=0) == 1)
                test(s(0, b=0) == 2)
           }
           fun s() {return 0}
           fun s(a=0){return 1}
           fun s(a,b=0){return 2}
        """
        )
    }

    @Test
    fun testSimilarSignatures() {
        val thrown = assertFails {
            eval(
                """
            fun main() {}
            fun same(a=0,b=0) {}
            fun same(a,b) {}
        """
            )
        }
        assertTrue(thrown.message!!.contains("Two functions with same signature"))

        val thrown2 = assertFails {
            eval(
                """
            fun main() {}
            fun same(a=0,b=0) {}
            fun same(a,b=0) {}
        """
            )
        }
        assertTrue(thrown2.message!!.contains("Two functions with same signature"))
    }

    @Test
    fun testDefaultParamOrder() {
        val thrown = assertFails {
            eval(
                """
                fun main(a = 0, b) {}
            """
            )
        }
        assertTrue(thrown.message!!.contains("Default params should be after other"))
    }

    @Test
    fun testIncorrectArgsOrder() {
        val thrown = assertFails {
            eval(
                """
                fun someFunc(a, b) {}
                fun main() {
                    someFunc(b = 1, 2)
                }
            """
            )
        }
        assertTrue(thrown.message!!.contains("Named args should be after other"))
    }

    @Test
    fun testIncorrectParam() {
        val thrown = assertFails {
            eval(
                """
                fun main(a.b.c) {}
            """
            )
        }
        assertTrue(thrown.message!!.contains("Expected identifier as function parameter"))
    }

    @Test
    fun twoMains() {
        val thrown = assertFails {
            eval(
                """
                fun main(){}
                fun main(a) {}
            """
            )
        }
        println(thrown.message)
        assertTrue(thrown.message!!.contains("Found 2 or more main functions"))
    }
}
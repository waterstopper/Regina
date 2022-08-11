import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class CallTest {
    @Test
    fun testChangingScope() {
        assertTrue(assertFails {
            eval(
                """
           fun main() {
            a = 1
            other()
           } 
            fun other() {
            test(a == 1)
            }
        """
            )
        }.message!!.contains("Not found a"))
    }

    @Test
    fun testSignature() {
        eval(
            """
           fun main() {
                test(f() == 0)
                test(f(1) == 1)
                test(f(a=0) == 1)
                test(f(0, b=0) == 2)
           }
           fun f() {return 0}
           fun f(a=0){return 1}
           fun f(a,b=0){return 2}
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
            fun same(y,z) {}
        """
            )
        }
        assertTrue(thrown.message!!.contains("Two functions with same signature"))

        val thrown2 = assertFails {
            eval(
                """
            fun main() {}
            fun same(a=0,b=0) {}
            fun same(y,z=0) {}
        """
            )
        }
        assertTrue(thrown2.message!!.contains("Two functions with same signature"))
    }

    @Test
    fun testDefaultParamOrder() {
        val thrown = assertFails { eval("fun main(a = 0, b) {}") }
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
        assertTrue(thrown.message!!.contains("Expected identifier or assignment as function parameter"))
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
        assertTrue(thrown.message!!.contains("Found 2 or more main functions"))
    }

    @Test
    fun alreadyAssignedParam() {
        val thrownArr = listOf(assertFails { eval("fun main(){f(a=1,a=1)}; fun f(a,b){}") },
            assertFails { eval("fun main(){f(1,a=1)}; fun f(a,b){}") },
            assertFails { eval("fun main(){f(b=1,b=1)}; fun f(a,b){}") })
        for (exception in thrownArr) {
            println(exception.message)
            assertTrue(exception.message!!.contains("Argument already assigned"))
        }
    }
}

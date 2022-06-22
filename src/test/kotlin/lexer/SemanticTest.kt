package lexer

import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class SemanticTest {
    @Test
    fun testSameFunction() {
        val thrown = assertFails {
            eval(
                """
            fun main() {}
            fun one() {}
            fun one() {
                a = 0
            }
        """
            )
        }
        assertTrue(thrown.message!!.contains("Two functions with same signature"))
    }

    @Test
    fun testSameClass() {
        val thrown = assertFails {
            eval(
                """
            class A{}
            class A{a = 0}
            fun main() {}
        """
            )
        }
        assertTrue(thrown.message!!.contains("Two classes with same name"))
    }

    @Test
    fun testSameObject() {
        val thrown = assertFails {
            eval(
                """
            object A{}
            object A{a = 0}
            fun main() {}
        """
            )
        }
        assertTrue(thrown.message!!.contains("Two objects with same name"))
    }

    @Test
    fun testAssignmentTopLevel() {
        val thrown = assertFails {
            eval(
                """
            fun main() {}
            a = 0
        """
            )
        }
        assertTrue(thrown.message!!.contains("Only class, object or function can be top level declaration"))
    }

    @Test
    fun testCallTopLevel() {
        val thrown = assertFails {
            eval(
                """
            fun main() {}
            main()
        """
            )
        }
        println(thrown.message)
        assertTrue(thrown.message!!.contains("Only class, object or function can be top level declaration"))
    }
}

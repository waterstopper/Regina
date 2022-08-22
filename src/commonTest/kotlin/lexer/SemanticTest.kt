package lexer

import evaluation.Evaluation.eval
import preload
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class SemanticTest {
    @BeforeTest
    fun addImports() {
        preload(listOf("std/geometry2D.rgn"))
    }

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
        assertTrue(thrown.message!!.contains("Only class, object or function can be top level declaration"))
    }

    @Test
    fun failLeftIsUnassignableInAssignment() {
        val thrown = assertFails { eval("fun main() {[] = 2}") }
        assertTrue(thrown.message!!.contains("Left operand is not assignable"))
    }

    @Test
    fun failFunctionAndClassNotFound() {
        // removed because by adding `type()` function, it is almost
        // impossible to say if an expression cannot be a constructor
//        val notFoundInFile = assertFails { eval("fun main() { notFound() } ") }
//        assertTrue(notFoundInFile.message!!.contains("No class and function found"))

        val notFoundInPrimitive = assertFails { eval("fun main() {a=1;a.notFound()}") }
        assertTrue(notFoundInPrimitive.message!!.contains("does not contain function"))

        val notFoundInType = assertFails {
            eval(
                """
           fun main() {
                a = A()
                a.f()
           }
            class A {
                a = 1
            }
        """
            )
        }
        assertTrue(notFoundInType.message!!.contains("Class `A` does not contain function"))

        val notFoundInObject = assertFails {
            eval(
                """
            fun main() {
                A.f()
            }
            object A {
                a = 1
            }
        """
            )
        }
        assertTrue(notFoundInObject.message!!.contains("Object `A` does not contain function"))
    }

    @Test
    fun invocationInLink() {
        eval(
            """
            fun main() {
                test(B.objectFun() == 1)
                a = A()
                test(a.getMe().a.getMe().iter == 1)
                test(firstInvocationFun().a.iter == 1)
            }
            class A {
                iter = if(parent == 0) 0 else parent.iter + 1
                a = if(iter < 5) A() else 0
                
                fun getMe() {return this}
            }
           
            object B {
                fun objectFun() {return 1}
            }
           
            fun firstInvocationFun() {return A()}
        """
        )
    }

    @Test
    fun twoSameImports() {
        val thrown = assertFails {
            eval(
                """
            import std.geometry2D as geom
            fun main() {}
            import std.geometry2D as g
        """
            )
        }
        assertTrue(thrown.message!!.contains("Same import found above"))
    }
}

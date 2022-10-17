package node

import evaluation.Evaluation.eval
import preload
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class LinkTest {
    @BeforeTest
    fun preloadFiles() {
        preload(
            listOf(
                "std/geometry2D.rgn"
            )
        )
    }

    @Test
    fun doubleIndexedLink() {
        eval(
            """
        fun main() {
            a = A()
            a.p["a"]["b"] = 1
        } 
        
        class A{
            p = {"a":{}}
        }
        """
        )
    }

    @Test
    fun unassignedNonTypeTest() {
        eval(
            """
            fun main() {
                fsd = fsd()
            }
            class fsd {
                a = []
                b = a.joinToString(", ")
            }
        """
        )
    }

    @Test
    fun testSetIndex() {
        eval(
            """
        fun main() {
            arr = [[0], [0]]
            arr[1][0] = 1
            test(arr[1][0] == 1)
            a = A()
            a.arr[0] = {1:2}
            test(a.arr[0] == {1:2})
            test(arr == [[0], [1]])
            
            b = [A()]
            b[0].arr[0] = {1:2, 3:4}
            test(b[0].arr[0] == {1:2, 3:4})
        }
        
        class A {
            arr = [1, 2]
        }
        """
        )
    }

    @Test
    fun testNullableLink() {
        eval(
            """
        fun main() {
            a = A()
            a.b = 1
            print(a.b)
            print(a.b?.c)
            print(a.b?.c)
            print(a.b?.c.d)
            test(a.b?.c.d == null)
            test(a.b?.c?.d == null)
        }
        
        class A {}
        """
        )
    }

    @Test
    fun testLinkOnTheLeftOfAssignment() {
        val thrown = assertFails {
            eval(
                """
            fun main() {
                a = A()
                test(a.n() == [[1]])
                a.n()[0][0] = 2
            }
            
            class A {
                fun n() {
                    return [[1]]
                }
            }
        """
            )
        }
        assertTrue(thrown.message!!.contains("Invocation or ternary cannot be on the left of the assignment"))
        val thrown2 = assertFails {
            eval(
                """
            fun main() {
                a = A()
                a?.b = 1
            }
            class A {}
        """
            )
        }
        assertTrue(thrown2.message!!.contains("Null safe calls are prohibited on the left of the assignment"))
    }

    @Test
    fun testImportObject() {
        eval(
            """
            import std.geometry2D as geom
            
            fun main() {
                a = A()
                print(geom.Constants.PI)
            }
            
            class A {
                iter = (parent?.iter ?? 0) + 1
            }
        """
        )
    }

    @Test
    fun testSameNamedLink() {
        eval("""
            fun main() {
                a = A()
            }
            
            class A {
                ps = createPs()
                
                fun createPs() {
                    b = B()
                    props = []
                    i = 0
                    print(b.props[i])
                }
            }
            
            class B {
                props = [1,2,3]
            }
        """)
    }
}

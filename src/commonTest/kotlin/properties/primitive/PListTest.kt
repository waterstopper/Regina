package properties.primitive

import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class PListTest {
    @Test
    fun testList() {
        eval(
            """
        fun main() {
            a = []
            a.add(a, 0)
            a.add([1,2])
            b = a.sorted()
            log(b)
            b.add(A())
            log(str(b))
            log(str(a))
            test(str(a)=="[this, [1, 2]]")
            test(a[0][0][1]==[1, 2])
            a.removeAt(0)
            test(a==[[1,2]])
            a.remove([1,2])
            test(a==[])
            test(!a.has(0))
            a.add(0)
            test(a.has(0))
            test(a.size == 1)
            test([1,2,3].has(1))
            
            a.clear()
            test(a == [])
        }
        
        class A {
            iter = (parent?.iter ?? 0) + 1
            next = if(iter < 5) A() else 0
        }
        """
        )
    }

    @Test
    fun testListEquals() {
        eval(
            """
        fun main() {
            aInst = A()
            a = [aInst]
            b = [aInst]
            test(a == b)
            a[0].s = 1
            test(b[0].s == 1)
            test(a == b)
            
            c = [A()]
            c[0].b = B()
            c[0].b.a = c[0]
            d = c.sorted()
            c.add(c[0].b)
            d.add(c[0].b)
            test(c == d)
        }
        class A {
            a = 0
        }
        
        class B {}
        """
        )
    }

    @Test
    fun testListSort() {
        eval(
            """
           fun main() {
               a = Obj
               arr = [3, 2, 1]
               test(str([Obj,type(Cls()),[],1,2,{1:2}, ZObj,[1,2], {}, 0.1, type(Zcls())].sorted()) \
                    == "[0.1, 1, 2, [], [1, 2], {}, {1=2}, Cls, Zcls, Obj-Object, ZObj-Object]")
               test(arr.sorted() == [1,2,3])
           }
         object Obj {}
         object ZObj {}
         class Cls {}
         class Zcls {}
        """
        )
    }

    @Test
    fun testJoinToString() {
        eval(
            """
            fun main() {
                a = [1,2,3]
                test(a.joinToString() == "1, 2, 3")
                test(a.joinToString(separator="\n") == "1\n2\n3")
                test(a.joinToString(separator="\"") == "1\"2\"3")
            }
        """
        )
    }

    @Test
    fun indexListNotInteger() {
        val thrown = assertFails {
            eval(
                """
            fun main() {
                a=[1,2,3]
                a[a]
            }
            """
            )
        }
        assertTrue(thrown.message!!.contains("Expected integer as index"))
    }

    @Test
    fun indexOutOfBounds() {
        val thrownNegative = assertFails {
            eval(
                """
            fun main() {
                a=[1,2,3]
                a[-1]
            }
            """
            )
        }
        assertTrue(thrownNegative.message!!.contains("Index out of bounds"))

        val thrownBigger = assertFails {
            eval(
                """
            fun main() {
                a=[1,2,3]
                a[3]
            }
            """
            )
        }
        assertTrue(thrownBigger.message!!.contains("Index out of bounds"))
    }

    @Test
    fun testImplicitList() {
        eval(
            """
           fun main() {
            test((getArray() + 0).sorted() == [0, 1, 2, 3])
           }
           fun getArray() {return [1, 3, 2]}
        """
        )
    }
}
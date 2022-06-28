package properties.primitive

import evaluation.Evaluation
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class PArrayTest {
    @Test
    fun testArray() {
        Evaluation.eval(
            """
        fun main() {
            a = []
            a.add(a, 0)
            a.add([1,2])
            test(str(a)=="[this, [1, 2]]")
            test(a[0][0][1]==[1,2])
            a.removeAt(0)
            test(a==[[1,2]])
            a.remove([1,2])
            test(a==[])
            test(!a.has(0))
            a.add(0)
            test(a.has(0))
            test(a.size == 1)
            test([1,2,3].has(1))
        }
        """
        )
    }

    @Test
    fun testArraySort(){ // TODO test joinToString
        Evaluation.eval(
            """
           fun main() {
               a = Obj
               arr = [3, 2, 1]
               log([Obj,Cls(),[],1,"2",{1:2}, ZObj,[1,2], {}, 0.1, Zcls()].sorted())
               test([Obj,Cls(),[],1,"2",{1:2}, ZObj,[1,2], {}, 0.1, Zcls()].sorted() \
                    == [0.1, 1, "2",[], [1, 2], {}, {1:2}, Cls(), Zcls(), Obj, ZObj])
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
    fun indexArrayNotInteger() {
        val thrown = assertFails {
            Evaluation.eval(
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
            Evaluation.eval(
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
            Evaluation.eval(
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
}
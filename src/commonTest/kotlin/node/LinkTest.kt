package node

import evaluation.Evaluation.eval
import preload
import kotlin.test.BeforeTest
import kotlin.test.Test

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
    fun testSetIndex() {
        eval(
            """
        fun main() {
            arr = [[0], [0]]
            log(arr[0][0])
            arr[1][0] = 1
            test(arr[1][0] == 1)
            a = A()
            a.arr[0] = {1:2}
            test(a.arr[0] == {1:2})
            
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
        eval("""
        fun main() {
            a = A()
            a.b = 1
            log(a.b)
            log(a.b?.c)
            log(!a.b?.c)
            log(!a.b?.c.d)
            test(!a.b?.c.d)
            test(!a.b?.c?.d)
        }
        
        class A {}
        """)
    }

    @Test
    fun testImportObject() {
        eval("""
            import std.geometry2D as geom
            
            fun main() {
            a = A()
                log(geom.Constants.PI)
            }
            
            class A {
                iter = parent?.iter + 1
            }
        """)
    }
}
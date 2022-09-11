package node

import evaluation.Evaluation.eval
import kotlin.test.Test

class LinkTest {
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
            test(!a.b?.c.d)
            test(!a.b?.c?.d)
        }
        
        class A {}
        """)
    }
}
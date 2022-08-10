package properties.primitive

import evaluation.Evaluation.eval
import kotlin.test.Test


class PDictionaryTest {
    @Test
    fun testDictionary() {
        eval(
            """
           fun main() {
                b = {1:2, 2:2}
                log(b)
                log(b.values)
                log("before")
                log(b.keys)
                log(1)
                test(b.values == [2, 2])
                log(2)
               // log(b.keys)
                test(b.keys == [1,2])
                a = {}
                a[1] = 2
                a[2] = 3
                log(a[1])
                test(a[1] == 2)
                test(a[2] == 3)
                test(a.size == 2)
                test(a.values == [2,3])
                test(a.remove(1) == 2)
                test(a[1] == 0)
                test(a.size == 1)
                log(a.keys)
                test(a.keys == [2])
                test(a.values == [3])
                
           } 
        """
        )
    }
}
package properties.primitive

import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class PNumberTest {
    @Test
    fun testNumber() {
        eval(
            """
            fun main() {
                test((1+1) is Int)
                test(0 == 0.0)
                
                test(0.MIN_VALUE == -2147483648)
                test(0.MAX_VALUE == 2147483647)
                test(0 is Int)
                test(0.1 is Double)
                test(0.0 is Int) // important test
                test(0.1 !is Int)
                
                test((-0.2.abs()) == -0.2)
                test((-0.2).abs() == 0.2)
                
                test(0.max(-1) == 0)
                test(0.max(0.MAX_VALUE + 1) == 0)
                test(0.min(0.MAX_VALUE + 1) == 0.MIN_VALUE)
                
                test(2.pow(3) == 8)
                test(2.pow(-3) == 0.125)
            }
        """
        )
    }

    @Test
    fun testDouble() {
        eval(
            """
            fun main() {
                test(2.345453.round(12) == 2.345453)
                test(12345.0.round(-2) == 12300)
                test(12365.0.round(-2) == 12400.0)
                test(0.2.round() == 0.0)
                test(-0.5.round() == -1.0)
                test(-1.125.round(2) == -1.13)
            }
        """
        )
    }

    @Test
    fun failBigNumber() {
        val thrownArr = listOf(assertFails {
            eval("fun main() { a = 2147483648 }")
        },
            assertFails {
                eval("fun main() { a = -2147483649 }")
            })
        for (exception in thrownArr)
            assertTrue(exception.message!!.contains("Integer can be in range"))
    }
}
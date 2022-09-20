package properties.primitive

import evaluation.Evaluation.eval
import kotlin.test.Test


class PrimitiveTest {
    @Test
    fun testVoidFunctionAsVariableIsZero() {
        eval(
            """
            fun main() {
                a = void()
                test(a == null)
            }
            fun void() {return}
        """
        )
    }
}
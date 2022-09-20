package node

import evaluation.Evaluation.eval
import kotlin.test.Test

class OperatorTest {
    @Test
    fun nullCoalescing() {
        eval(
            """
        fun main() {
            test(int("ab") ?? 3 == 3)
        } 
        """
        )
    }
}
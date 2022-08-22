import evaluation.Evaluation.eval
import kotlin.test.Test

class DefaultTest {
    @Test
    fun testTypeFunction() {
        eval(
            """
            fun main() {
            test(type(A()) == A)
            }
            class A {}
        """
        )
    }
}
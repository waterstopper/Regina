package evaluation

import evaluation.Evaluation.evaluate
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class EvaluationTest {
    @Test
    fun evaluateTest() = runBlocking {
        launch {
            evaluate("src/test/resources/testCode.rgn")
        }
        evaluate("src/test/resources/testCode.rgn")
    }
}

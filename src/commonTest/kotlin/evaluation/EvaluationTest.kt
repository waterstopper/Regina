package evaluation

import evaluation.Evaluation.evaluate
import preload
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class EvaluationTest {
    @BeforeTest
    fun preloadFiles() {
        preload(
            listOf(
                "src/commonTest/resources/testCode.rgn",
                "std/math.rgn",
                "std/svg.rgn",
                "std/geometry2D.rgn"
            )
        )
    }

    @Test
    fun evaluateTest() {
        evaluate("src/commonTest/resources/testCode.rgn")
    }
}

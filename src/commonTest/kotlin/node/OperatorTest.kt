package node

import evaluation.Evaluation.eval
import evaluation.Evaluation.evaluate
import preload
import kotlin.test.BeforeTest
import kotlin.test.Test

class OperatorTest {
    @BeforeTest
    fun preloadFiles() {
        preload(
            listOf(
                "src/commonTest/resources/isTest.rgn",
                "src/commonTest/resources/imported.rgn"
            )
        )
    }

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

    @Test
    fun isTest() {
        evaluate("src/commonTest/resources/isTest.rgn")
    }
}
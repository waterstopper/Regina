package evaluation

import evaluation.Evaluation.evaluate
import org.junit.Test
internal class EvaluationTest {
    @Test
    fun evaluateTest() {
        //  val text = File("src/test/resources/testCode").readText()
        //val s = readFile("src/test/resources/testCode.redi")
        //SemanticAnalyzer.initializeSuperTypes()
        evaluate("src/test/resources/testCode")
    }
}

package evaluation

import evaluation.Evaluation.evaluate
//import lexer.SemanticAnalyzer
import org.junit.Test
//import readFile

internal class EvaluationTest {
    @Test
    fun evaluateTest() {
        //  val text = File("src/test/resources/testCode").readText()
        //val s = readFile("src/test/resources/testCode.redi")
        //SemanticAnalyzer.initializeSuperTypes()
        evaluate("src/test/resources/testCode")
    }
}

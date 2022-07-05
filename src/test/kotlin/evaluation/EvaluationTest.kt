package evaluation

import evaluation.Evaluation.oldEvaluate
import lexer.SemanticAnalyzer
import org.junit.Test
import readFile

internal class EvaluationTest {

    @Test
    fun evaluateTest() {
        //  val text = File("src/test/resources/testCode").readText()
        val s = readFile("src/test/resources/testCode.redi")
        // println(s.treeView())
        SemanticAnalyzer.initializeSuperTypes()
        oldEvaluate(s, "testCode.redi")
    }
}

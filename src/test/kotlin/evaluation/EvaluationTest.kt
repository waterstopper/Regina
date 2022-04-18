package evaluation

import evaluation.Evaluation.evaluate
import org.junit.Test
import readFile


internal class EvaluationTest {

    @Test
    fun evaluateTest() {
      //  val text = File("src/test/resources/testCode").readText()
        val s = readFile("src/test/resources/testCode.redi")
        //println(s.treeView())
        evaluate(s,"testCode.redi")
    }
}
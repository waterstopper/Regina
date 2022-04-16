package evaluation

import evaluation.Evaluation.evaluate
import org.junit.Test
import readFile
import treeView


internal class EvaluationTest {

    @Test
    fun evaluateTest() {
      //  val text = File("src/test/resources/testCode").readText()
        val s = readFile("src/test/resources/testCode")
        //println(s.treeView())
        evaluate(s,"testCode")
    }
}
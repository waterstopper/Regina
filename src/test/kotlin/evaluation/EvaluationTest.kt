package evaluation

import lexer.Parser
import org.junit.Test

import java.io.File

internal class EvaluationTest {

    @Test
    fun evaluate() {
        val text = File("src/test/resources/testCode").readText()
        val s = Parser(text).statements()
        Evaluation.evaluate(s)
    }
}
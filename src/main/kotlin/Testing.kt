import evaluation.Evaluation
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible

fun main() = runBlocking {
    launch {
        Evaluation.evaluate("src/test/resources/testCode")
    }
    //Evaluation.evaluate("src/test/resources/std/mathTest.redi")
    Evaluation.evaluate("src/test/resources/testCode")
}
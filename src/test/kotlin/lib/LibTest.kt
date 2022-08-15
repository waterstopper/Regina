package lib

import evaluation.Evaluation.eval
import evaluation.Evaluation.evaluate
import kotlin.test.Test

class LibTest {
    @Test
    fun testMath() {
        evaluate("src/test/resources/std/mathTest.rgn")
    }

    @Test
    fun testGeometry() {
        evaluate("src/test/resources/std/geometry2DTest.rgn")
    }

    @Test
    fun testIO() {
        eval(
            """
            fun main() {
                test(!exists("test.txt"))
                write("Written to file.\nEnd of file.", "test.txt")
                test(read("test.txt") == "Written to file.\nEnd of file.")
                test(delete("test.txt"))
                test(!exists("test.txt"))
            }
        """
        )
    }
}
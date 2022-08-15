package lib

import evaluation.Evaluation.eval
import evaluation.Evaluation.evaluate
import preload
import kotlin.test.BeforeTest
import kotlin.test.Test

class LibTest {
    @BeforeTest
    fun preloadNeeded() {
        preload(
            listOf(
                "std/geometry2D.rgn",
                "std/math.rgn",
                "src/commonTest/resources/std/mathTest.rgn",
                "src/commonTest/resources/std/geometry2DTest.rgn"
            )
        )
    }

    @Test
    fun testMath() {
        evaluate("src/commonTest/resources/std/mathTest.rgn")
    }

    @Test
    fun testGeometry() {
        evaluate("src/commonTest/resources/std/geometry2DTest.rgn")
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
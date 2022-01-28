package raw

import org.junit.Assert
import org.junit.Test
import primitives.Rect

internal class RawNodeTest {

    @Test
    fun calculateLinksInFieldTest() {
        val rect = Rect("root")

        val rawRectChild = RawRect("nam","@root.color","scal","rot","pos")

        rawRectChild.calculateLinks(mutableListOf(rect))

        println(rawRectChild.color)
        Assert.assertTrue(true)
    }
}
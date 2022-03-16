package export

import Container
import org.junit.Test


internal class PainterTest{
    @Test
    fun draw(){
        Painter(Container("", null, mutableMapOf())).export()
    }
}
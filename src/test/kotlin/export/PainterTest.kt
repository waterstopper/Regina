package export

import OldContainer
import org.junit.Test


internal class PainterTest{
    @Test
    fun draw(){
        Painter(OldContainer("", null, mutableMapOf())).export()
    }
}
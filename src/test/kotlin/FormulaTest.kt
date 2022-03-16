import org.junit.Assert
import org.junit.Test

import org.junit.jupiter.api.Assertions.*

internal class FormulaTest {

    @Test
    fun getLinks() {
        Assert.assertEquals(
            Formula("@few+@name+@dqw.r3+@1ew.e").getLinks(),
            mutableListOf("few", "name", "dqw", "1ew")
        )

        Assert.assertEquals(
            Formula("1+42+eqw+eq").getLinks(),
            mutableListOf<String>()
        )
    }
}
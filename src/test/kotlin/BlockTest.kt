import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class BlockTest {
    @Test
    fun testPassingBlocks() {
        eval(
            """
           fun main() {a = (1-1)*3
                while(a < 5)
                    a = a + 1
                test(a == 5)
                b = 1
                if(true)
                    b = b + 1
                test(b == 2)}
        """
        )
    }

    @Test
    fun testOneLineBlocks() {
        eval(
            """
           fun main() {test(0==0)}
           fun someFunction() {a = 0    
           }
           class A{}
           class B{a=0}
           object C{}
           object D{a=0}
        """
        )
    }

    @Test
    fun testNoBlockFunction() {
        val thrown = assertFails {
            eval(
                """
           fun main()
                test(0==0)
        """
            )
        }
        assertTrue(thrown.message!!.contains("Expected a block start"))
    }

    @Test
    fun testNoBlockClass() {
        val thrown = assertFails { eval("class A") }
        assertTrue(thrown.message!!.contains("Expected a block start"))
    }

    @Test
    fun testNoBlockObject() {
        val thrown = assertFails { eval("object A") }
        assertTrue(thrown.message!!.contains("Expected a block start"))
    }
}

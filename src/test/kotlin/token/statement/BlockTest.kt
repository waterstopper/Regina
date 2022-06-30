package token.statement
import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class BlockTest {
    @Test
    fun failBlockWithinBlock(){
        val thrown = assertFails { eval("""
            fun main() {
             {}
            }
        """) }
        assertTrue(thrown.message!!.contains("Block within a block"))
    }
    @Test
    fun failBreakOutOfCycle() {
        val thrown = assertFails { eval("""
            fun main(){
            while(0)
                a = 1
             break
            }
        """) }
        assertTrue(thrown.message!!.contains("break out of cycle"))
    }

    @Test
    fun failContinueOutOfCycle() {
        val thrown = assertFails { eval("""
            fun main(){
            while(0)
                a = 1
             continue
            }
        """) }
        assertTrue(thrown.message!!.contains("continue out of cycle"))
    }

    @Test
    fun returnInCycle() {
        eval("""
            fun main() {test(funWithReturnInCycle() == 2)}
            fun funWithReturnInCycle() {while(1) return 2 }
        """)
    }

    @Test
    fun cycleTest() {
        eval("""
            fun main() {
            	arg = 0
            	if(true) {
            		while(arg < 10) {
            			if(arg > 5) 
            				break
            			test(arg <= 5)
            			arg = arg + 1
            			continue // leave before wrong test
            			test(1==2)
            		}
            	}
            }
        """)
    }
}
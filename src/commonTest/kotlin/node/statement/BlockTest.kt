package node.statement

import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class BlockTest {
    @Test
    fun testForeach() {
        eval("""
            fun main() {
                foreach(i in {1:2, 3:4}.entries) {
                    test(i["key"] % 2 == 1)
                    test(i["value"] % 2 == 0)
                }
                index = 0
                foreach   (i in [1,2,3]  ) {
                    index = index + 1
                    test(index == i)
                }
                index = 0
                foreach(i in "123") {
                    index = index + 1
                    test(index == int(i))
                }
                
            } 
        """)
    }

    @Test
    fun testForeachMutable() {
        eval("""
            fun main() {
                arr = [[], []]
                foreach(i in arr) {
                    i.add(1)
                }
                test(arr == [[1], [1]])
            } 
        """)
    }

    @Test
    fun testRange() {
        eval("""
            fun main() {
                foreach(i in range(1, 2, 3))
                    test(i == 1)
                visits = 0
                foreach(i in range(10, 2)) {
                    test(i <= 10 && i >= 2)
                    visits = visits + 1
                }
                test(visits == 9)
                visits = 0
                foreach(i in range(10, 2, 2)) {
                    test(i <= 10 && i >= 2)
                    visits = visits + 1
                }
                test(visits == 5)
                foreach(i in range(2, 10, 2)) {
                    test(i <= 10 && i >= 2)
                    visits = visits + 1
                }
                test(visits == 10)
            }
        """)
    }

    @Test
    fun testPassingBlocks() {
        eval(
            """
           fun main() {a = (1-1)*3
                while(a < 5)a = a + 1
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

    @Test
    fun failBlockWithinBlock() {
        val thrown = assertFails {
            eval(
                """
            fun main() {
             {}
            }
        """
            )
        }
        assertTrue(thrown.message!!.contains("Block within a block"))
    }

    @Test
    fun failBreakOutOfCycle() {
        val thrown = assertFails {
            eval(
                """
            fun main(){
            while(0)
                a = 1
             break
            }
        """
            )
        }
        assertTrue(thrown.message!!.contains("break out of cycle"))
    }

    @Test
    fun failContinueOutOfCycle() {
        val thrown = assertFails {
            eval(
                """
            fun main(){
            while(0)
                a = 1
             continue
            }
        """
            )
        }
        assertTrue(thrown.message!!.contains("continue out of cycle"))
    }

    @Test
    fun returnInCycle() {
        eval(
            """
            fun main() {test(funWithReturnInCycle() == 2)}
            fun funWithReturnInCycle() {while(1) return 2 }
        """
        )
    }

    @Test
    fun cycleTest() {
        eval(
            """
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
        """
        )
    }
}
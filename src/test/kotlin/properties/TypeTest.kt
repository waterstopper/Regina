package properties

import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

class A {
    val a = A()
}

class TypeTest {
    @Test
    fun testBidirectionalPropertyResolving() {
        eval("""
           fun main() {
                a = A()
                log("Built")
                log(a.str())
           }
           class A {
                iter = if(parent == 0) 0 else parent.iter + 1
                next = if(iter < 1) A() else 0
                fromNext = if(next == 0) -1 else next.fromNext - 1
              
              fun str() {
                  return "\n"+iter + " " + fromNext + (if(next == 0) "" else next.str()) 
              }
              
              fun withCycle() {
                while(1) {
                    a.b.c
                }
              }
           }
        """)
    }

    @Test
    fun sameProperty() {
        val thrown = assertFails {  eval("""
            fun main(){}
            class A {
                a = 0
                a = 1
            }
        """)}
        assertTrue(thrown.message!!.contains("Same property found above"))
    }

    @Test
    fun testEquals() {
        eval("""
            fun main() {
                first = A()
                firstOtherLink = first
                second = A()
                test(first != second)
                test(first == firstOtherLink)
                first.s = 1
                test(first == firstOtherLink)
                test(firstOtherLink.s == 1)
                firstOtherLink.a = "a"
                test(first.a == "a")
                
                firstSecondOther = changeB(first)
                test(firstOtherLink.b == "b")
                test(firstSecondOther == first)
                test(firstSecondOther == firstOtherLink)
            }
            class A {
                a = 0
                b = 1
            }
            fun changeB(aInstance) {aInstance.b = "b"
                return aInstance
            }
        """)
    }

    @Test
    fun checkInheritance() {
        eval("""
           class A {
                a = 2
                fun a() {return "a"}
           }
           class B:A {
            b = 3
            fun b() {return b}
            fun a() {return "b"}
           }
           fun main() {
            b = B()
            log(b.properties)
            log(b.a())
           }
        """)
    }
}
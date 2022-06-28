package properties

import evaluation.Evaluation.eval
import kotlin.test.Test

class TypeTest {
    //Test
    fun testBidirectionalPropertyResolving() {
        eval("""
           fun main() {
                a = A()
                log(a.str())
           } 
           class A {
                iter = if(parent == 0) 0 else parent.iter + 1
                next = if(iter < 2) A() else 0
              //  fromNext = if(iter == 10) -1 else next.fromNext - 1
              
              fun str() {
              log(iter)
              return iter + " " + if(next == 0) "" else next.str()
              }
           }
        """)
    }
}
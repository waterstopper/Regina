package properties

import evaluation.Evaluation.eval
import kotlin.test.Test


class ObjectTest {
    @Test
    fun testProperty() {
        eval("""
            fun main() {
                test(Obj.a == 1)
                test(Obj.a == 1)
                Obj.a = 2
                test(Obj.a == 2)
            }
            object Obj {
                a = initCalledOnce()
            }
            object beforeObj {
                isInit = 1
            }
            fun initCalledOnce() {
                test(beforeObj.isInit == 1)
                beforeObj.isInit = 2
                return 1
            }
        """)
    }

    // @Test
    fun testDynamicProperties() {
        eval("""
           fun main() {
                log(O.a)
           }
           object O {
                a = a
                b = a
           }
           object P {
               b = O.a
           }
        """)
    }
}
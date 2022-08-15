package properties

import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue


class ObjectTest {
    @Test
    fun testProperty() {
        eval(
            """
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
                test(beforeObj.isInit == 2)
                return 1
            }
        """
        )
    }

    @Test
    fun failInheritance() {
        val thrown = assertFails {
            eval(
                """
           class C {}
           object Ok {}
           object O: C {}
           fun main() {}
        """
            )
        }
        assertTrue(thrown.message!!.contains("Object cannot be inherited"))
    }
}
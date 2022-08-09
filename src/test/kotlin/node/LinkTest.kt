package node

import evaluation.Evaluation.eval
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue


class LinkTest {
    @Test
    fun testNotFound() {
        val thrown = assertFails { eval("""
           fun main() {
            a = A()
            log(a.s)
           }
            class A {
                 a = 0
            }
        """) }
        assertTrue(thrown.message!!.contains("Link not resolved"))
    }
}
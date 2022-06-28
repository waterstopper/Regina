package properties.primitive

import evaluation.Evaluation.eval
import kotlin.test.Test

class PStringTest {
    @Test
    fun testString() {
        eval("""
            fun main() {
            s = "abcde"
            test(s.substring(2) == "cde")
            test(s.substring(1,2) == "b")
            test(s.substring(0,5) == "abcde")
            
            test(s.replace("a","b") == "bbcde")
            test(s.replace("abc","") == "de")
            test(s.replace("_", "A") == "abcde")
            
            test(s.reversed() == "edcba")
            test(s\
                .substring(0,5) \ // abcde
                .substring(1) \ // bcde
                .replace("b", "ab") \ // abcde
                .reversed() == "edcba")
            test(s.replace("","_") == "_a_b_c_d_e_")
            
            test(s.lowercase() == "abcde")
            test("A_$#$@DDkofe".uppercase() == "A_$#$@DDKOFE")
            test("A_$#$@DDkofe".lowercase() == "a_$#$@ddkofe")
            
            test(array(s) == ["a", "b", "c", "d", "e"])
            }
        """)

    }
}
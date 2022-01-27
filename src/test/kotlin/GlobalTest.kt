import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import rand.Rand
import rand.RandInt

internal class GlobalTest {
//    @Test
//    fun transformTest() {
//        Assert.assertEquals(
//            Global.transform("1//2>=3234   >=   3"),
//            "1\\2]3234]3"
//        )
//        Assert.assertEquals(
//            Global.transform("1==2==3>=4<=5<=6>=7!=8!=9!=10"),
//            "1=2=3]4[5[6]7!8!9!10"
//        )
//        Assert.assertEquals(Global.transform("1&&2&&3||4||5||6"), "1&2&3|4|5|6")
//    }

    @Test
    fun evaluateTest() {
        Assert.assertEquals(Global.evaluate("1+3*5"), 16)

        Assert.assertEquals(Global.evaluate("(1+3)*5"), 20)
        Assert.assertEquals(Global.evaluate("(1+3)*5.0"), 20.0)

        Assert.assertEquals(Global.evaluate("5.5//3"), 1)
        Assert.assertEquals(Global.evaluate("11/3/2"), 1)

        Assert.assertEquals(Global.evaluate("1==0+1"), 1)
        Assert.assertEquals(Global.evaluate("(1==0)+1"), 1)

        Assert.assertEquals(Global.evaluate("1--1"), 2)
        Assert.assertEquals(Global.evaluate("-1"), -1)

        Assert.assertEquals(Global.evaluate("2 * (3-(1+(5/2)-2)+-2) - 3"), -3)
        Assert.assertEquals(Global.evaluate("2 * (3-(1+(5/2)-2)+-2.0) - 3"), -3.0)

        Assert.assertEquals(Global.evaluate("3==2?-6:-7"), -7)
        Assert.assertEquals(Global.evaluate("0?1 : 1?4:5"), 4)
        Assert.assertEquals(Global.evaluate("(5?3:2)?6:7"),6)
    }

    @Test
    fun evaluateRandomTest(){
        val rand: Rand = RandInt(2,3)
        Assert.assertTrue((2..3).contains(Global.evaluate(Json.encodeToString(rand))))
    }
}
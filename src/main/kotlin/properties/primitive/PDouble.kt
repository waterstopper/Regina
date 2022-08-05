package properties.primitive

import evaluation.FunctionFactory.getDouble
import evaluation.FunctionFactory.getInt
import properties.EmbeddedFunction
import properties.Type
import utils.Utils.parseAssignment
import utils.Utils.toProperty
import kotlin.math.pow
import kotlin.math.roundToInt

class PDouble(value: Double, parent: Type?) : PNumber(value, parent) {
    override fun getIndex() = 3
    override fun getPValue() = value as Double

    companion object {
        fun initializeDoubleProperties() {
            val d = PDouble(0.0, null)
            setProperty(d, "MIN_VALUE") { Double.MIN_VALUE.toProperty() }
            setProperty(d, "MAX_VALUE") { Double.MAX_VALUE.toProperty() }
        }

        fun initializeEmbeddedDoubleFunctions() {
            val d = PDouble(0.0, null)
            setFunction(
                d,
                EmbeddedFunction(
                    "round",
                    listOf(),
                    listOf(parseAssignment("digits = 0"))
                ) { token, args ->
                    val number = getDouble(token, "this", args)
                    val digits = getInt(token, "digits", args)
                    if (digits.getPValue() < 0) {
                        val divisor = 10.0.pow(-digits.getPValue())
                        (number.getPValue() / divisor).roundToInt() * divisor
                    } else String.format("%.${digits}f", number.getPValue()).replace(',', '.').toDouble()
                }
            )
        }
    }
}

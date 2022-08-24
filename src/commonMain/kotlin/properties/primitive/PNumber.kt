package properties.primitive

import References
import evaluation.FunctionFactory
import evaluation.FunctionFactory.getIdent
import evaluation.FunctionFactory.getNumber
import isInt
import properties.EmbeddedFunction
import properties.Type
import round
import utils.Utils.castToNumber
import utils.Utils.unaryMinus
import utils.Utils.unifyPNumbers
import kotlin.math.pow
import kotlin.math.roundToInt

open class PNumber(value: Number, parent: Type?) : Primitive(value, parent) {
    override fun getIndex() = 1
    override fun getPValue() = value as Number

    override fun equals(other: Any?): Boolean {
        if (other !is PNumber)
            return false
        return getPValue().toDouble() == other.getPValue().toDouble()
    }

    override fun hashCode(): Int {
        return getPValue().hashCode()
    }

    override fun toDebugClass(references: References): Any {
        throw Exception("class is not instantiable")
    }

    companion object {
        fun initializeEmbeddedNumberFunctions() {
            val n = PNumber(0, null)
            setFunction(
                n,
                EmbeddedFunction("abs") { _, args ->
                    val number = castToNumber(args.getPropertyOrNull("this")!!)
                    if (number.getPValue().toDouble() >= 0) number.getPValue() else -number.getPValue()
                }
            )
            setFunction(
                n,
                EmbeddedFunction("min", listOf("other")) { token, args ->
                    val (number, other) = unifyPNumbers(
                        args.getPropertyOrNull("this")!!,
                        getIdent(token, "other", args),
                        token
                    )
                    if (isInt(number))
                        (number as Int).coerceAtMost(other as Int)
                    else (number as Double).coerceAtMost(other as Double)
                }
            )
            setFunction(
                n,
                EmbeddedFunction("max", listOf("other")) { token, args ->
                    val (number, other) = unifyPNumbers(
                        args.getPropertyOrNull("this")!!,
                        getIdent(token, "other", args),
                        token
                    )
                    if (isInt(number))
                        (number as Int).coerceAtLeast(other as Int)
                    else (number as Double).coerceAtLeast(other as Double)
                }
            )
            setFunction(
                n,
                EmbeddedFunction("pow", listOf("deg")) { token, args ->
                    val number = castToNumber(args.getPropertyOrNull("this")!!)
                    val deg = getNumber(token, "deg", args)
                    number.getPValue().toDouble().pow(deg.getPValue().toDouble())
                }
            )
            setFunction(
                n,
                EmbeddedFunction(
                    "round",
                    namedArgs = listOf("digits = 0")
                ) { token, args ->
                    val number = getNumber(token, "this", args)
                    val digits = FunctionFactory.getInt(token, "digits", args)
                    if (digits.getPValue() < 0) {
                        val divisor = 10.0.pow(-digits.getPValue())
                        (number.getPValue().toDouble() / divisor).roundToInt() * divisor
                    } else round(number.getPValue().toDouble(), digits.getPValue())
                }
            )
            setFunction(
                n,
                EmbeddedFunction(
                    "intDivide",
                    listOf("divisor")
                ) { token, args ->
                    val number = getNumber(token, "this", args)
                    val divisor = getNumber(token, "divisor", args)
                    number.getPValue().toInt() / divisor.getPValue().toInt()
                }
            )
        }
    }
}

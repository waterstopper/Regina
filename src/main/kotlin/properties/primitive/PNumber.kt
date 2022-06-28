package properties.primitive

import evaluation.FunctionFactory.getIdent
import evaluation.FunctionFactory.getNumber
import properties.EmbeddedFunction
import properties.Type
import token.Token
import utils.Utils.castToNumber
import utils.Utils.unaryMinus
import utils.Utils.unifyPNumbers
import kotlin.math.pow

open class PNumber(value: Number, parent: Type?) : Primitive(value, parent) {
    override fun getIndex() = 1
    override fun getPValue() = value as Number

    override fun equals(other: Any?): Boolean {
        if (other !is PNumber)
            return false
        return getPValue().toDouble() == other.getPValue().toDouble()
    }

    override fun hashCode(): Int {
        return getPValue().toDouble().hashCode()
    }

    companion object {
        fun initializeEmbeddedNumberFunctions() {
            val n = PNumber(0, null)
            setFunction(
                n,
                EmbeddedFunction("abs", listOf()) { token, args ->
                    val number = castToNumber(args.getPropertyOrNull("this")!!)
                    if (number.getPValue().toDouble() >= 0) number.getPValue() else -number.getPValue()
                }
            )
            setFunction(
                n,
                EmbeddedFunction("min", listOf(Token(value = "other"))) { token, args ->
                    val (number, other) = unifyPNumbers(
                        args.getPropertyOrNull("this")!!,
                        getIdent(token, "other", args),
                        token
                    )
                    if (number is Int)
                        number.coerceAtMost(other as Int)
                    else (number as Double).coerceAtMost(other as Double)
                }
            )
            setFunction(
                n,
                EmbeddedFunction("max", listOf(Token(value = "other"))) { token, args ->
                    val (number, other) = unifyPNumbers(
                        args.getPropertyOrNull("this")!!,
                        getIdent(token, "other", args),
                        token
                    )
                    if (number is Int)
                        number.coerceAtLeast(other as Int)
                    else (number as Double).coerceAtLeast(other as Double)
                }
            )
            setFunction(
                n,
                EmbeddedFunction("pow", listOf(Token(value = "deg"))) { token, args ->
                    val number = castToNumber(args.getPropertyOrNull("this")!!)
                    val deg = getNumber(token, "deg", args)
                    number.getPValue().toDouble().pow(deg.getPValue().toDouble())
                }
            )
        }
    }
}

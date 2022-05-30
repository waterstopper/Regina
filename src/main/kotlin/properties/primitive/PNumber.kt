package properties.primitive

import evaluation.FunctionFactory.getIdent
import lexer.ExpectedTypeException
import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Type
import token.Token
import utils.Utils.unaryMinus
import utils.Utils.unifyNumbers
import utils.Utils.unifyPNumbers
import kotlin.math.pow

open class PNumber(value: Number, parent: Type?) : Primitive(value, parent) {
    override fun getIndex() = 1
    override fun getPValue() = value as Number


    fun setFunction(embeddedFunction: EmbeddedFunction) {
        Primitive.functions[1].add(embeddedFunction)
        Primitive.functions[2].add(embeddedFunction)
    }

    companion object {
        fun initializeEmbeddedNumberFunctions() {
            val n = PNumber(0, null)
            setFunction(n, EmbeddedFunction("abs", listOf()) { token, args ->
                val number = args.getPropertyOrNull("this")!!
                if (number is PNumber)
                    if (number.getPValue().toDouble() >= 0) number.getPValue() else -number.getPValue()
                else throw PositionalException("Expected number", token)
            })
            setFunction(n, EmbeddedFunction("min", listOf(Token(value = "other"))) { token, args ->
                val (number, other) = unifyNumbers(
                    args.getPropertyOrNull("this")!!,
                    getIdent(token, "other", args),
                    token
                )
                if (number is Int)
                    number.coerceAtMost(other as Int)
                else (number as Double).coerceAtMost(other as Double)
            })
            setFunction(n, EmbeddedFunction("max", listOf(Token(value = "other"))) { token, args ->
                val (number, other) = unifyPNumbers(
                    args.getPropertyOrNull("this")!!,
                    getIdent(token, "other", args),
                    token
                )
                if (number is Int)
                    number.coerceAtLeast(other as Int)
                else (number as Double).coerceAtLeast(other as Double)
            })
            setFunction(n, EmbeddedFunction("pow", listOf(Token(value = "deg"))) { token, args ->
                val number = args.getPropertyOrNull("this")!!
                val deg = getIdent(token, "deg", args)
                if (deg !is PNumber)
                    throw ExpectedTypeException(listOf(PNumber::class), token)
                when (number) {
                    is PInt -> number.getPValue().toDouble().pow(deg.getPValue().toDouble()).toInt()
                    is PDouble -> number.getPValue().pow(deg.getPValue().toDouble())
                    else -> throw ExpectedTypeException(listOf(PNumber::class), token)
                }
            })
        }
    }
}
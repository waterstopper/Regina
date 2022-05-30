package properties.primitive

import evaluation.FunctionFactory.getIdent
import lexer.ExpectedTypeException
import lexer.Parser
import properties.EmbeddedFunction
import properties.Type
import token.statement.Assignment
import utils.Utils.toProperty


class PDouble(value: Double, parent: Type?) : PNumber(value, parent) {
    override fun getIndex() = 3
    override fun getPValue() = value as Double

    companion object {
        fun initializeDoubleProperties() {
            val d = PDouble(0.0, null)
            setProperty(d, "MIN_VALUE") { p: Primitive -> Double.MIN_VALUE.toProperty() }
            setProperty(d, "MAX_VALUE") { p: Primitive -> Double.MAX_VALUE.toProperty() }
        }

        fun initializeEmbeddedDoubleFunctions() {
            val d = PDouble(0.0, null)
            setFunction(d, EmbeddedFunction(
                "round",
                listOf(),
                listOf(Parser("digits = 0").statements().first() as Assignment)
            ) { token, args ->
                val number = getIdent(token, "this", args)
                if (number !is PDouble)
                    throw ExpectedTypeException(listOf(PDouble::class), token)
                val digits = getIdent(token, "digits", args)
                if (digits !is PInt)
                    throw ExpectedTypeException(listOf(PInt::class), token)
                String.format("%.${digits}f", number.getPValue()).replace(',','.').toDouble()
            })
        }
    }
}
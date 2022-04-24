package properties.primitive

import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import properties.Type
import utils.Utils.unaryMinus

abstract class PNumber(value: Number, parent: Type?) : Primitive(value, parent) {
    companion object {
        val functions = initializeEmbeddedNumberFunctions() + Primitive.functions

        private fun initializeEmbeddedNumberFunctions(): MutableList<Function> {
            val res = mutableListOf<Function>()
            res.add(EmbeddedFunction("abs", listOf(), { token, args ->
                val number = args.getVariable("(this)")
                if (number is Number)
                    if (number.toDouble() >= 0) number else -number
                else throw PositionalException("Expected number", token)
            }))
            return res
        }
    }
}
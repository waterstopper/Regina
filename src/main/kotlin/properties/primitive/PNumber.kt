package properties.primitive

import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import properties.Type
import token.Token
import utils.Utils.unaryMinus

abstract class PNumber(value: Number, parent: Type?) : Primitive(value, parent) {
    override fun getFunction(token: Token): Function =
        Primitive.functions[getIndex()].find { it.name == token.value }
            ?: functions.find { it.name == token.value }
            ?: throw PositionalException("Number does not contain `${token.value}` function", token)


    fun setFunction(embeddedFunction: EmbeddedFunction) {
        Primitive.functions[1].add(embeddedFunction)
        Primitive.functions[2].add(embeddedFunction)
    }

    companion object {
        val functions = initializeEmbeddedNumberFunctions()

        private fun initializeEmbeddedNumberFunctions(): MutableList<Function> {
            val res = mutableListOf<Function>()
            res.add(EmbeddedFunction("abs", listOf(), { token, args ->
                val number = args.getVariable("(this)")
                if (number is PNumber)
                    if ((number.getPValue() as Number).toDouble() >= 0) (number as Primitive).getPValue() else -((number.getPValue()) as Number)
                else throw PositionalException("Expected number", token)
            }, 0..0))
            return res
        }
    }
}
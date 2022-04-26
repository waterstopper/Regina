package token.operator

import lexer.Parser
import lexer.PositionalException
import properties.Type
import properties.primitive.PArray
import properties.primitive.PDouble
import properties.primitive.PInt
import properties.primitive.PString
import table.SymbolTable
import token.Token
import utils.Utils.toInt
import utils.Utils.toVariable

class TypeOperator(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?
) : Operator(symbol, value, position, bindingPower, nud, led, std) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (symbol) {
            "is" -> evaluateTypeCheck(symbolTable).toInt()
            "isnot" -> (!evaluateTypeCheck(symbolTable)).toInt()
            else -> throw PositionalException("unknown word for operator", this)
        }
    }

    private fun evaluateTypeCheck(symbolTable: SymbolTable): Boolean {
        val checked = left.evaluate(symbolTable).toVariable(left)
        return when (right.value) {
            "String" -> checked is PString
            "Int" -> checked is PInt
            "Double" -> checked is PDouble
            "Array" -> checked is PArray
            else -> {
                val type = right.evaluate(symbolTable)
                if (checked is Type && type is Type
                    && checked.assignments.isEmpty()
                    && type.assignments.isEmpty()
                )
                    return checked.inherits(type)
                throw PositionalException(
                    "expected class instance or primitive as left operator and class name as right operator",
                    this
                )
            }
        }

    }
}
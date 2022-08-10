package node.operator

import lexer.PositionalException
import node.Node
import properties.Type
import properties.primitive.*
import table.SymbolTable
import utils.Utils.toInt
import utils.Utils.toVariable

class TypeOperator(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node>
) : Operator(symbol, value, position, children) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (symbol) {
            "is" -> evaluateTypeCheck(symbolTable).toInt()
            "!is" -> (!evaluateTypeCheck(symbolTable)).toInt()
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
                if (checked is Primitive)
                    return false
                val type = right.evaluate(symbolTable)
                if (checked is Type && type is Type &&
                    checked.assignments.isEmpty() &&
                    type.getProperties().getPValue().isEmpty()
                )
                    return checked.inherits(type)
                throw PositionalException(
                    "Expected class instance or primitive as left operator and class name as right operator",
                    this
                )
            }
        }
    }
}

package node.operator

import lexer.PositionalException
import node.Node
import properties.Null
import properties.Object
import properties.Type
import properties.primitive.*
import table.SymbolTable
import utils.Utils.toPInt
import utils.Utils.toVariable

class TypeOperator(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node>
) : Operator(symbol, value, position, children) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (symbol) {
            "is" -> evaluateTypeCheck(symbolTable).toPInt()
            "!is" -> (!evaluateTypeCheck(symbolTable)).toPInt()
            else -> throw PositionalException("unknown word for operator", symbolTable.getFileTable().filePath, this)
        }
    }

    private fun evaluateTypeCheck(symbolTable: SymbolTable): Boolean {
        val checked = left.evaluate(symbolTable).toVariable(left)
        return when (right.value) {
            "String" -> checked is PString
            "Int" -> checked is PInt
            "Double" -> checked is PDouble
            "List" -> checked is PList
            "Number" -> checked is PNumber
            "Class" -> checked is Type
            "Primitive" -> checked is Primitive
            else -> {
                if (checked is Primitive || checked is Null)
                    return false
                val type = right.evaluate(symbolTable)
                if (checked is Type && checked !is Object
                    && type is Type && type !is Object
                    && checked.assignments.isEmpty() &&
                    type.getProperties().getPValue().isEmpty()
                )
                    return checked.inherits(type)
                throw PositionalException(
                    "Expected class instance or primitive as left operator and class name as right operator",
                    symbolTable.getFileTable().filePath,
                    this
                )
            }
        }
    }
}

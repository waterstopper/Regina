package node.operator

import lexer.PositionalException
import node.Identifier
import node.Link
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
            "Dictionary" -> checked is PDictionary
            "Number" -> checked is PNumber
            "Class" -> checked is Type
            "Primitive" -> checked is Primitive
            else -> {
                if (checked is Primitive || checked is Null) {
                    return false
                }
                if (right !is Link && right !is Identifier) {
                    throw PositionalException(
                        "Currently `is` and `!is` expression support type and imported type on the right-hand side",
                        symbolTable.getFileTable().filePath,
                        right
                    )
                }
                val type = if (right is Identifier) symbolTable.getUncopiedTypeOrNull(right) else getType(
                    symbolTable,
                    right as Link
                )
                if (checked is Type && checked !is Object &&
                    type is Type && type !is Object &&
                    checked.index != 0 &&
                    type.index == 0
                ) {
                    return checked.inherits(type)
                }
                throw PositionalException(
                    "Expected class instance or primitive as left operator and class name as right operator",
                    symbolTable.getFileTable().filePath,
                    this
                )
            }
        }
    }

    private fun getType(symbolTable: SymbolTable, link: Link): Type? {
        if (link.children.size != 2 || link.left !is Identifier || link.right !is Identifier) {
            throw PositionalException(
                "Expected link in form of `importName.typeName`",
                symbolTable.getFileTable().filePath,
                link
            )
        }
        val import = symbolTable.getImport(link.left)
        return symbolTable.changeFile(import).getUncopiedTypeOrNull(link.right)
    }
}

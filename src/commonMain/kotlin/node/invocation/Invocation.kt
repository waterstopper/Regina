package node.invocation

import lexer.PositionalException
import node.Linkable
import node.Node
import node.statement.Assignment
import properties.Type
import table.SymbolTable

open class Invocation(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    children: List<Node> = emptyList()
) : Node(symbol, value, position, children.toMutableList()), Linkable {
    val name: Node
        get() = left

    override fun evaluate(symbolTable: SymbolTable): Any {
        throw PositionalException(
            "Invocations should be replaced with Calls or Constructors",
            symbolTable.getFileTable().filePath,
            this
        )
    }

    override fun findUnassigned(symbolTable: SymbolTable, parent: Type): Pair<Type, Assignment>? {
        for (arg in children.subList(1, children.size)) {
            val found = left.findUnassigned(symbolTable, parent)
            if (found != null) {
                return found
            }
        }
        return null
    }
}

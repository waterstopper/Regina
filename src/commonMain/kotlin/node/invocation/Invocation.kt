package node.invocation

import lexer.PositionalException
import node.Linkable
import node.Node
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
            symbolTable.getFileTable().filePath, this
        )
    }
}

package properties

import node.Node
import node.statement.Assignment
import table.SymbolTable

class EmbeddedFunction(
    name: String,
    args: List<Node>,
    namedArgs: List<Assignment> = listOf(),
    private val execute: (node: Node, arguments: SymbolTable) -> Any,
) : Function(name, args, namedArgs, Node()) {
    fun executeFunction(node: Node, symbolTable: SymbolTable): Any = execute(node, symbolTable)
}

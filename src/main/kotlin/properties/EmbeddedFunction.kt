package properties

import node.Identifier
import node.Node
import table.SymbolTable
import utils.Utils.parseAssignment

class EmbeddedFunction(
    name: String,
    args: List<String> = listOf(),
    namedArgs: List<String> = listOf(),
    private val execute: (node: Node, arguments: SymbolTable) -> Any,
) : Function(name, args.map { Identifier(it) }, namedArgs.map { parseAssignment(it) }, Node()) {
    fun executeFunction(node: Node, symbolTable: SymbolTable): Any = execute(node, symbolTable)
}

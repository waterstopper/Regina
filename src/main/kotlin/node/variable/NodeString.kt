package node.variable

import node.Node
import table.SymbolTable

class NodeString(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
) : Node(symbol, value, position) {

    override fun evaluate(symbolTable: SymbolTable): Any = value
}

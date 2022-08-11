package node.variable

import node.Node
import table.SymbolTable

class NodeNumber(value: String, position: Pair<Int, Int>, val number: Number) :
    Node("(NUMBER)", value, position) {

    override fun evaluate(symbolTable: SymbolTable) = number
}

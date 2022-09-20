package node.variable

import node.Node
import table.SymbolTable
import utils.Utils.NULL

class NodeNull(position: Pair<Int, Int>) : Node("(NULL)", "null", position) {
    override fun evaluate(symbolTable: SymbolTable) = NULL
}

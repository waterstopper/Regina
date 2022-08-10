package node.variable

import node.Node
import table.SymbolTable

class NodeNumber(value: String, position: Pair<Int, Int>) :
    Node("(NUMBER)", value, position) {
    var number: Number? = null

    override fun evaluate(symbolTable: SymbolTable): Number {
        if (number == null)
            return if (value.contains(".")) value.toDouble() else value.toInt() // TODO make parseStatement using Semantic analyzer to remove this line
        return number!!
    }
}

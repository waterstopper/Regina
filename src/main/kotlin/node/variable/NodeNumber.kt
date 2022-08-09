package node.variable

import lexer.Parser
import table.SymbolTable
import node.Node

class NodeNumber(value: String, position: Pair<Int, Int>) :
    Node("(NUMBER)", value, position, 0, { t: Node, _: Parser -> t }, null, null) {
    var number: Number? = null

    override fun evaluate(symbolTable: SymbolTable): Number {
        if (number == null)
            return if (value.contains(".")) value.toDouble() else value.toInt() // TODO make parseStatement using Semantic analyzer to remove this line
        return number!!
    }
}

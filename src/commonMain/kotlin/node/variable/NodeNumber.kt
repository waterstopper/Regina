package node.variable

import node.Node
import properties.primitive.PDouble
import properties.primitive.PInt
import table.SymbolTable

class NodeNumber(value: String, position: Pair<Int, Int>, val number: Number, val isDouble: Boolean = false) :
    Node("(NUMBER)", value, position) {

    override fun evaluate(symbolTable: SymbolTable) =
        if (isDouble) PDouble(number.toDouble()) else PInt(number.toInt())
}

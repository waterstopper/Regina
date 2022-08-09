package node.statement

import table.SymbolTable
import node.Node

class WordStatement(node: Node) :
    Node(node.symbol, node.value, node.position, node.bindingPower, node.nud, node.led, node.std) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (symbol) {
            "while" -> {
            }
            "return" -> {
                if (children.size == 0)
                    Unit
                else left.evaluate(symbolTable)
            }
            "break" -> {
            }
            "continue" -> {
            }
            else -> {
            }
        }
    }
}

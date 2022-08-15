package node.statement

import node.Node
import table.SymbolTable

class WordStatement(node: Node) :
    Node(node.symbol, node.value, node.position, node.children) {
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

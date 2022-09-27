package node.statement

import lexer.PositionalException
import node.Node
import table.SymbolTable

class WordStatement(node: Node) :
    Node(node.symbol, node.value, node.position, node.children) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (symbol) {
            "while" -> throw PositionalException("while out of place", symbolTable.getFileTable().filePath, this)
            "return" -> {
                if (children.size == 0) {
                    Unit
                } else left.evaluate(symbolTable)
            }
            "break" -> throw PositionalException("break out of place", symbolTable.getFileTable().filePath, this)
            "continue" -> throw PositionalException("continue out of place", symbolTable.getFileTable().filePath, this)
            else -> throw PositionalException("Unexpected token", symbolTable.getFileTable().filePath, this)
        }
    }
}

package token

import node.Node
import node.statement.WordStatement

class TokenWordStatement(node: Token) :
    Token(node.symbol, node.value, node.position, node.bindingPower, node.nud, node.led, node.std) {
    override fun toNode(filePath: String): Node {
        return WordStatement(Node(symbol, value, position, children.map { it.toNode(filePath) }.toMutableList()))
    }
}

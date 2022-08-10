package token

import node.Node
import node.operator.NodeTernary

class TokenTernary(node: Token) :
    Token(node.symbol, node.value, node.position, node.bindingPower, node.nud, node.led, node.std) {
    override fun toNode(): Node {
        return NodeTernary(Node(symbol, value, position, children.map { it.toNode() }.toMutableList()))
    }
}
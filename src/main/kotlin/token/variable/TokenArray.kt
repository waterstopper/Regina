import node.Node
import node.variable.NodeArray
import token.Token

class TokenArray(node: Token) : Token(
    node.symbol,
    node.value,
    node.position,
    node.bindingPower,
    node.nud,
    node.led,
    node.std,
    node.children
) {
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun toNode(): Node {
        return NodeArray(Node(symbol, value, position, children.map { it.toNode() }.toMutableList()))
    }
}

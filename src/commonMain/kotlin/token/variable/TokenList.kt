import node.Node
import node.variable.NodeList
import token.Token

class TokenList(node: Token) : Token(
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

    override fun toNode(filePath: String): Node {
        return NodeList(Node(symbol, value, position, children.map { it.toNode(filePath) }.toMutableList()))
    }
}

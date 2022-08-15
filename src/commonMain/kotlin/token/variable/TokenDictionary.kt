import node.Node
import node.variable.NodeDictionary
import token.Token

class TokenDictionary(node: Token) : Token(
    node.symbol,
    node.value,
    node.position,
    node.bindingPower,
    node.nud,
    node.led,
    node.std,
    node.children
) {
//    init {
//        this.children.clear()
//        this.children.addAll(children)
//    }

    override fun toNode(): Node {
        return NodeDictionary(Node(symbol, value, position, children.map { it.toNode() }.toMutableList()))
    }
}

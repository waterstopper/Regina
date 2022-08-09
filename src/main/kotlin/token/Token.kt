package token

import lexer.Parser
import node.Node

open class Token(
    var symbol: String = "",
    var value: String = "",
    val position: Pair<Int, Int> = Pair(0, 0),
    val bindingPower: Int = 0, // precedence priority
    var nud: ((node: Node, parser: Parser) -> Node)? = null, // null denotation: values, prefix operators
    var led: ((node: Node, parser: Parser, node2: Node) -> Node)? = null, // left denotation: infix and suffix operators
    var std: ((node: Node, parser: Parser) -> Node)? = null, // statement denotation
    val children: MutableList<Node> = mutableListOf()
) {
    open fun toNode(): Node {
        TODO()
    }
}
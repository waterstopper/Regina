package token

import lexer.Parser
import node.Node

open class Token(
    var symbol: String = "",
    var value: String = "",
    val position: Pair<Int, Int> = Pair(0, 0),
    val bindingPower: Int = 0, // precedence priority
    var nud: ((token: Token, parser: Parser) -> Token)? = null, // null denotation: values, prefix operators
    var led: ((token: Token, parser: Parser, node2: Token) -> Token)? = null, // left denotation: infix and suffix operators
    var std: ((token: Token, parser: Parser) -> Token)? = null, // statement denotation
    val children: MutableList<Token> = mutableListOf()
) {
    val left: Token
        get() = children[0]
    val right: Token
        get() = children[1]

    open fun toNode(): Node {
        return Node(symbol = symbol, value = value, children = children.map { it.toNode() }.toMutableList())
    }
}
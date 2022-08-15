package token

import lexer.Parser
import node.Node
import node.statement.Assignment

class Assignment(
    symbol: String = "",
    value: String,
    position: Pair<Int, Int> = Pair(0, 0),
    bindingPower: Int = 0,
    nud: ((node: Token, parser: Parser) -> Token)? = null,
    led: ((node: Token, parser: Parser, node2: Token) -> Token)? = null,
    std: ((node: Token, parser: Parser) -> Token)? = null,
    children: MutableList<Token> = mutableListOf()
) : Operator(symbol, value, position, bindingPower, nud, led, std) {
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun toNode(): Node {
        return Assignment(symbol, value, position, children.map{it.toNode()}.toMutableList())
    }
}

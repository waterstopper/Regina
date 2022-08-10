package token

import lexer.Parser
import node.Node
import node.operator.Operator

open class Operator(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((node: Token, parser: Parser) -> Token)?,
    led: (
        (
        node: Token, parser: Parser, node2: Token
    ) -> Token
    )?,
    std: ((node: Token, parser: Parser) -> Token)?
) : Token(symbol, value, position, bindingPower, nud, led, std) {
    override fun toNode(): Node {
        return Operator(symbol, value, position, children.map{it.toNode()}.toMutableList())
    }
}

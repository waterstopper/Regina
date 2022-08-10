package token

import lexer.Parser
import node.Linkable
import node.Node
import node.invocation.Invocation

open class Invocation(
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
    std: ((node: Token, parser: Parser) -> Token)?,
) : Token(symbol, value, position, bindingPower, nud, led, std), Linkable {
    val name: Token
        get() = left

    override fun toNode(): Node {
        return Invocation(symbol, value, position, children.map { it.toNode() })
    }
}

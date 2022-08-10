package token

import lexer.Parser
import node.Identifier
import node.Linkable
import node.Node

open class TokenIdentifier(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((node: Token, parser: Parser) -> Token)?,
    led: ((node: Token, parser: Parser, node2: Token) -> Token)?,
    std: ((node: Token, parser: Parser) -> Token)?
) : Token(symbol, value, position, bindingPower, nud, led, std), Linkable {

    override fun toNode(): Node {
        return Identifier(symbol, value, position)
    }
}

package token.variable

import lexer.Parser
import node.Node
import node.variable.NodeString
import token.Token

class TokenString(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: (
        (
        token: Token, parser: Parser, node2: Token
    ) -> Token
    )?,
    std: ((token: Token, parser: Parser) -> Token)?
) : Token(symbol, value, position, bindingPower, nud, led, std) {

    override fun toNode(filePath: String): Node {
        return NodeString(symbol, value, position)
    }
}

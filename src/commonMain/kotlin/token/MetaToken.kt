package token

import lexer.Parser
import node.Meta
import node.Node

class MetaToken(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    nud: ((node: Token, parser: Parser) -> Token)? = { t: Token, _: Parser -> t }
) : Token(symbol, value, position, nud = nud) {
    override fun toNode(filePath: String): Node {
        return Meta(symbol, value, position)
    }
}
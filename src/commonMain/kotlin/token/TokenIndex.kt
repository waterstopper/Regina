package token

import lexer.Parser
import node.Linkable
import node.Node
import node.operator.Index
import properties.primitive.*

/**
 * Format: `a[i]` -  `[]` is index, `a` is indexed value
 *  ([PArray] or [PDictionary]) and `i` is [PInt] or key of dictionary.
 *
 * Token that represents taking value from collection by index or key
 */
class TokenIndex(
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
    children: List<Token> = listOf()
) : Token(symbol, value, position, bindingPower, nud, led, std), Linkable {
    constructor(node: Token) : this(
        node.symbol,
        node.value,
        node.position,
        node.bindingPower,
        node.nud,
        node.led,
        node.std,
        node.children
    )

    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun toNode(): Node {
        return Index(symbol, value, position, children.map { it.toNode() })
    }
}

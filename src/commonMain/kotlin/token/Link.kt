package token

import lexer.Parser
import node.Link
import node.Node
import node.invocation.Invocation
import node.operator.Index

/**
 * Format: `a.b.c.d` - `a`, `b`, `c` and `d` are children of link
 *
 * Represents tokens separated by dots. These tokens are link children. In Regina, links have the following purposes:
 * 1. A property of class, object or a primitive: `Point.x` or `Segment.parent.iter`
 * 2. A function of class, object or a primitive: `Double.round()`
 * 3. Reference to a class, object or a function from another file: `importedFile.className`
 * 4.
 * That's why links are complex, they should be carefully evaluated and assigned.
 *
 * Link invariants:
 * * First token in link might be anything
 * * n-th token is [Linkable]: [Identifier], [Invocation] or [Index]
 */
open class Link(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((node: Token, parser: Parser) -> Token)?,
    led: ((node: Token, parser: Parser, node2: Token) -> Token)?,
    std: ((node: Token, parser: Parser) -> Token)?,
    children: List<Token> = listOf()
) : Token(symbol, value, position, bindingPower, nud, led, std) {
    constructor(node: Token) : this(
        node.symbol, node.value,
        node.position, node.bindingPower,
        node.nud, node.led,
        node.std, node.children
    )

    init {
        if (children.isNotEmpty()) {
            this.children.clear()
            this.children.addAll(children)
        }
    }

    override fun toNode(filePath: String): Node {
        return Link(symbol, value, position, children.map { it.toNode(filePath) })
    }
}

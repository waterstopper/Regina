package node

import lexer.Parser
import lexer.PositionalException

open class Declaration(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((node: Node, parser: Parser) -> Node)?,
    led: (
        (
        node: Node, parser: Parser, node2: Node
    ) -> Node
    )?,
    std: ((node: Node, parser: Parser) -> Node)?,
    children: List<Node>
) : Node(symbol, value, position, bindingPower, nud, led, std) {
    constructor(node: Node) : this(
        node.symbol,
        node.value,
        node.position,
        node.bindingPower,
        node.nud,
        node.led,
        node.std,
        node.children
    )

    val name: Node
        get() = getDeclarationName()
    val supertype: Node
        get() = children[1]

    private fun getDeclarationName(): Node {
        return when (symbol) {
            "fun" -> {
                var res = left
                while (res is Link)
                    res = res.right
                res
            }
            "object" -> left
            "class" -> getSupertype(left).first
            "import" -> if (children.size != 1 || children.first() !is Identifier) throw PositionalException(
                "Expected file name",
                this
            ) else children.first()
            else -> throw PositionalException("Unregistered declaration", this)
        }
    }

    private fun getSupertype(node: Node): Pair<Node, Node?> {
        return if (node.value == ":")
            Pair(node.left, node.right)
        else Pair(node, null)
    }
}

class ImportDeclaration(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((node: Node, parser: Parser) -> Node)?,
    led: (
        (
        node: Node, parser: Parser, node2: Node
    ) -> Node
    )?,
    std: ((node: Node, parser: Parser) -> Node)?,
) : Declaration(symbol, value, position, bindingPower, nud, led, std, listOf()) {

}
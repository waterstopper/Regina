package node.invocation

import lexer.Parser
import lexer.PositionalException
import table.SymbolTable
import node.Linkable
import node.Node

open class Invocation(
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
    children: List<Node> = emptyList()
) : Node(symbol, value, position, bindingPower, nud, led, std), Linkable {
    val name: Node
        get() = left

    override fun evaluate(symbolTable: SymbolTable): Any {
        throw PositionalException(
            "Invocations should be replaced with Calls or Constructors",
            this, file = symbolTable.getFileTable().fileName
        )
    }
}

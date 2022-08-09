package node.variable

import lexer.Parser
import table.SymbolTable
import node.Node

class NodeString(
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
    std: ((node: Node, parser: Parser) -> Node)?
) : Node(symbol, value, position, bindingPower, nud, led, std) {

    override fun evaluate(symbolTable: SymbolTable): Any = value
}

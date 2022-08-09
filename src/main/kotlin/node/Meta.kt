package node

import lexer.Parser
import properties.primitive.PInt
import table.SymbolTable

class Meta(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    nud: ((node: Node, parser: Parser) -> Node)? = { t: Node, _: Parser -> t }
) : Node(symbol, value, position, nud = nud) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        println(symbolTable.toDebugString())
        readLine()
        return PInt(0, null)//super.evaluate(symbolTable)
    }
}
package node

import properties.primitive.PInt
import readLine
import table.SymbolTable

class Meta(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
) : Node(symbol, value, position) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        println(symbolTable.toDebugString())
        readLine()
        return PInt(0, null)//super.evaluate(symbolTable)
    }
}
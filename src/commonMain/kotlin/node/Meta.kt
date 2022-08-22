package node

import Message
import properties.primitive.PInt
import readLine
import sendMessage
import table.SymbolTable

class Meta(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
) : Node(symbol, value, position) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        sendMessage(Message("debug", symbolTable.getDictionaryFromTable())) // TODO to debug string
        println(symbolTable.toDebugString())
        readLine()
        return PInt(0, null)//super.evaluate(symbolTable)
    }
}
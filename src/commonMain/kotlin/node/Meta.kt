package node

import Message
import readLine
import sendMessage
import table.SymbolTable
import utils.Utils.NULL

class Meta(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
) : Node(symbol, value, position) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        val content = symbolTable.getDictionaryFromTable()
        content["@position"] = position
        content["@file"] = symbolTable.getFileTable().filePath
        sendMessage(Message("debug", content))
       // readLine()
        return NULL
    }
}
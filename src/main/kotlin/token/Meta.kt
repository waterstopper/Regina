package token

import lexer.Parser
import properties.primitive.PInt
import table.SymbolTable

class Meta(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    nud: ((token: Token, parser: Parser) -> Token)? = { t: Token, _: Parser -> t }
) : Token(symbol, value, position, nud = nud) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        println(symbolTable.toDebugString())
        readLine()
        return PInt(0, null)//super.evaluate(symbolTable)
    }
}
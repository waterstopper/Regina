package token


import lexer.Parser
import properties.primitive.Primitive
import table.SymbolTable

open class TokenIdentifier(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?
) : Token(symbol, value, position, bindingPower, nud, led, std) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        val variable = symbolTable.getIdentifier(this)
        if (variable is Primitive)
            return variable.value
        return variable
    }
}


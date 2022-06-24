package token.invocation

import lexer.Parser
import lexer.PositionalException
import table.SymbolTable
import token.Linkable
import token.Token

open class Invocation(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: (
        (
        token: Token, parser: Parser, token2: Token
    ) -> Token
    )?,
    std: ((token: Token, parser: Parser) -> Token)?,
    children: List<Token> = emptyList()
) : Token(symbol, value, position, bindingPower, nud, led, std), Linkable {
    val name: Token
        get() = left

    override fun evaluate(symbolTable: SymbolTable): Any {
        throw PositionalException("FDIEFJ ${this.left.value}", this, file = symbolTable.getFileTable().fileName)
        // return left.evaluate(symbolTable)
    }
}

package token.invocation

import Argumentable
import lexer.Parser
import table.SymbolTable
import token.Token

open class Invocation(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?,
    children: List<Token> = emptyList()
) : Token(symbol, value, position, bindingPower, nud, led, std), Argumentable {
    val name: Token
        get() = left

    override fun evaluate(symbolTable: SymbolTable): Any {
        return left.evaluate(symbolTable)
    }
}
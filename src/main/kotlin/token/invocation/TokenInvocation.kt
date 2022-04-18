package token.invocation

import lexer.Parser
import SymbolTable
import token.Token

class TokenInvocation(
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
        return left.evaluate(symbolTable)
    }
}
package token

import SymbolTable
import evaluation.FunctionEvaluation.toVariable
import evaluation.ValueEvaluation
import lexer.Parser

class TokenArray(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?, children: List<Token>
) : TokenIdentifier(symbol, value, position, bindingPower, nud, led, std) {
    constructor(token: Token) : this(
        token.symbol,
        token.value,
        token.position,
        token.bindingPower,
        token.nud,
        token.led,
        token.std,
        token.children
    )
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        return children.map { ValueEvaluation.evaluateValue(it, symbolTable).toVariable(it) }.toMutableList()
    }


}
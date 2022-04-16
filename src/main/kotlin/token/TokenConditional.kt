package token

import SymbolTable
import evaluation.FunctionEvaluation
import evaluation.ValueEvaluation.toBoolean
import lexer.Parser
import lexer.PositionalException

class TokenConditional(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((token: Token, parser: Parser, token2: Token) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?
) : Token(symbol, value, position, bindingPower, nud, led, std) {
    constructor(token: Token) : this(
        token.symbol,
        token.value,
        token.position,
        token.bindingPower,
        token.nud,
        token.led,
        token.std
    )

    override fun evaluate(symbolTable: SymbolTable): Any {
        val condition = left
        val trueBlock = right
        if (condition.evaluate(symbolTable).toBoolean(condition))
            return FunctionEvaluation.evaluateBlock(trueBlock, symbolTable)
        else if (children.size == 3)
            return FunctionEvaluation.evaluateBlock(children[2], symbolTable)
        return Unit
    }
}
package token

import evaluation.ValueEvaluation.eq
import evaluation.ValueEvaluation.neq
import evaluation.ValueEvaluation.plus
import evaluation.ValueEvaluation.toInt
import lexer.Parser
import lexer.PositionalException
import SymbolTable

open class TokenOperator(
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
        return when (value) {
            "+" -> left.evaluate(symbolTable) + right.evaluate(symbolTable)
            "==" -> left.evaluate(symbolTable).eq(right.evaluate(symbolTable)).toInt()
            "!=" -> left.evaluate(symbolTable).neq(right.evaluate(symbolTable)).toInt()
            else -> throw PositionalException("operator $value not implemented", this)
        }
    }
}
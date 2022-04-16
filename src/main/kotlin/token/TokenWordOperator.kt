package token

import SymbolTable
import evaluation.ValueEvaluation
import evaluation.ValueEvaluation.toInt
import lexer.Parser
import lexer.PositionalException

class TokenWordOperator(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?
) : TokenOperator(symbol, value, position, bindingPower, nud, led, std) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (symbol) {
            "is" -> evaluateTypeCheck(symbolTable).toInt()
            "!is" -> (!evaluateTypeCheck(symbolTable)).toInt()
            else -> throw PositionalException("unknown word for operator", this)
        }
    }

    private fun evaluateTypeCheck(symbolTable: SymbolTable): Boolean {
        val checked = ValueEvaluation.evaluateValue(left, symbolTable)
        val type = ValueEvaluation.evaluateValue(right, symbolTable)
        if (checked is SymbolTable.Type && type is SymbolTable.Type
            && checked.assignments.isEmpty()
            && type.symbolTable.isEmpty()
        )
            return checked.typeName == type.typeName
        throw PositionalException("expected class instance as left operator and class name as right operator", this)
    }
}
package token.operator

import lexer.PositionalException
import table.SymbolTable
import token.Token

class TokenTernary(token: Token) :
    Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        if (children.size != 3)
            throw PositionalException("ternary if should have else branch", this)
        val condition = evaluateCondition(symbolTable)
        return if (condition != 0)
            right.evaluate(symbolTable)
        else children[2].evaluate(symbolTable)
    }

    fun evaluateCondition(symbolTable: SymbolTable): Any =
        left.evaluate(symbolTable)
}
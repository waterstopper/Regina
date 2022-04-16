package token

import SymbolTable
import lexer.PositionalException

class TokenTernary(token: Token) :
    Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        if (children.size != 3)
            throw PositionalException("ternary if should have else branch", this)
        return if (left.evaluate(symbolTable) != 0)
            right.evaluate(symbolTable)
        else children[2].evaluate(symbolTable)
    }
}
package token.statement

import table.SymbolTable
import token.Token

class TokenWordStatement(token: Token) :
    Token(token.symbol, token.value, token.position, token.bindingPower, token.nud, token.led, token.std) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        return when (symbol) {
            "while" -> {
            }
            "return" -> {
                if (children.size == 0)
                    Unit
                else left.evaluate(symbolTable)
            }
            "break" -> {
            }
            "continue" -> {
            }
            else -> {
            }
        }
    }
}
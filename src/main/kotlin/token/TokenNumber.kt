package token

import lexer.Parser
import SymbolTable

class TokenNumber(value: String, position: Pair<Int, Int>) :
    Token("(NUMBER)", value, position, 0, { t: Token, _: Parser -> t }, null, null) {

    override fun evaluate(symbolTable: SymbolTable): Number =
        if (value.contains(".")) value.toDouble() else value.toInt()
}
package token.variable

import lexer.Parser
import table.SymbolTable
import token.Token

class TokenNumber(value: String, position: Pair<Int, Int>) :
    Token("(NUMBER)", value, position, 0, { t: Token, _: Parser -> t }, null, null) {

    override fun evaluate(symbolTable: SymbolTable): Number =
        if (value.contains(".")) value.toDouble() else value.toInt()

//    override fun copy(): TokenAssignment = TokenAssignment(
//        symbol, value, position, bindingPower, nud, led, std, children.map { it.copy() }.toMutableList()
//    )
}

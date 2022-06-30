package token.variable

import table.SymbolTable
import token.Token
import utils.Utils.toVariable

class TokenArray(token: Token) : Token(
    token.symbol,
    token.value,
    token.position,
    token.bindingPower,
    token.nud,
    token.led,
    token.std,
    token.children
) {
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        return children.map { it.evaluate(symbolTable).toVariable(it) }.toMutableList()
    }
}

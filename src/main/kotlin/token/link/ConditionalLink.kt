package token.link

import table.SymbolTable
import token.Token
import utils.Utils.toVariable

/**
 * (if a else b).c
 * a and b are identifier tokens
 */
class ConditionalLink(token: Token) : Link(token) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        val eval = left.evaluate(symbolTable).toVariable(left)
        return VariableLink(this, eval)
    }
}
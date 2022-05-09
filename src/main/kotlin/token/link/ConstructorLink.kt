package token.link

import properties.Type
import table.SymbolTable
import token.Token
import token.invocation.Constructor


class ConstructorLink(token: Token, val type: Type) : Link(token) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        val typeTable = symbolTable.copy()
        val type = symbolTable.getType(this).copy()
        typeTable.addVariable(left.value, type)
        return (left as Constructor).evaluateType(type, typeTable)
    }
}
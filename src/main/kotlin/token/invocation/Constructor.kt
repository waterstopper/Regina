package token.invocation

import lexer.PositionalException
import properties.Type
import properties.Type.Companion.resolveTree
import properties.Type.Companion.resolving
import table.SymbolTable
import token.Identifier
import token.Token
import token.statement.Assignment
import utils.Utils.toProperty

class Constructor(
    token: Token
) : Invocation(
    token.symbol, token.value,
    token.position, token.bindingPower,
    token.nud, token.led, token.std,
    token.children
) {
    init {
        this.children.clear()
        this.children.addAll(token.children)
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        val type = symbolTable.getType(left)
        return evaluateType(type, symbolTable)
    }

    private fun evaluateType(type: Type, symbolTable: SymbolTable): Any {
        resolveArguments(type, symbolTable)
        return if (resolving) type else resolveTree(type, symbolTable.changeVariable(type).changeScope())
    }

    private fun resolveArguments(type: Type, symbolTable: SymbolTable) {
        for (arg in children.subList(1, children.size)) {
            if (arg !is Assignment)
                throw PositionalException("Expected assignment", arg)
            if (arg.left !is Identifier)
                throw PositionalException("Expected property name", arg)
            type.setProperty(arg.left.value, arg.right.evaluate(symbolTable).toProperty(arg.left, type))
            type.removeAssignment(arg.left)
        }
        type.setProperty("this", type)
    }
}

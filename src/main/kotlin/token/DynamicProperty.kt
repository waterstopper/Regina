package token

import properties.Type
import table.SymbolTable
import token.link.Link
import token.operator.Index
import token.statement.Assignment

/**
 * Identifier, Link, Index
 */
class DynamicProperty(private val wrapped: Token, private val parentType: Type) : Token(
    wrapped.symbol,
    wrapped.value,
    wrapped.position,
    wrapped.bindingPower,
    wrapped.nud,
    wrapped.led,
    wrapped.std
) {
    var resolved: Any? = null

    fun getAssignment(): Assignment {
        val parent = getParent()
        if (wrapped is Identifier)
            return parent.assignments[0]//parent.assignments.find { it. }
        return parent.assignments[0]
    }

    fun isResolved(): Any? = resolved

    fun assign(value: Any) {

    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        return wrapped.evaluate(symbolTable)
    }


    fun getParent(): Type {
        when (wrapped) {
            is Identifier -> parentType
            is Index -> {
            }
            is Link -> {
            }
        }
        throw Exception()
    }


}
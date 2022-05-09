package token.link

import lexer.PositionalException
import properties.Function
import table.SymbolTable
import token.Identifier
import token.invocation.Call
import utils.Utils.toVariable

class CallLink(token: Link, function: Function) : Link(token) {
    init {
        (left as Call).function = function
    }

    fun setFunction(function: Function) {
        (left as Call).function = function
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        val functionResult = left.evaluate(symbolTable).toVariable(this)
        when (getAfterDot()) {
            is Call -> {
                val function = functionResult.getFunction(getAfterDot())
                return if (right is CallLink) {
                    (right as CallLink).setFunction(function)
                    right.evaluate(symbolTable)
                } else {
                    (right as Call).function = function
                    right.evaluate(symbolTable)
                }
            }
            is Identifier -> {
                val property = functionResult.getProperty(getAfterDot())
                return if (right is Link) {
                    val res = VariableLink(right as Link, property)
                    val variableTable = symbolTable.copy()
                    variableTable.addVariable(getAfterDot().value, property)
                    res.evaluate(symbolTable)
                } else {
                    (right as Identifier).variable = property
                    right.evaluate(symbolTable)
                }
            }
        }
        throw PositionalException("Expected property, call or constructor", getAfterDot())
    }
}

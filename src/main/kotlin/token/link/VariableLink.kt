package token.link

import lexer.PositionalException
import properties.Type
import properties.Variable
import table.SymbolTable
import token.Identifier
import token.invocation.Call
import token.invocation.Invocation
import token.statement.Assignment

/**
 * Variable or property
 */
class VariableLink(token: Link, var variable: Variable?) :
    Link(token) {
    override fun evaluate(symbolTable: SymbolTable): Any {
        val varTable = if (variable is Type) symbolTable.changeType(variable!! as Type) else symbolTable
        when (getAfterDot()) {
            is Call -> {
                varTable.addVariable("(this)", variable!!)
                val function = variable!!.getFunction((getAfterDot() as Call).name)
                (getAfterDot() as Call).argumentsToParameters(function, symbolTable, varTable)
                return (getAfterDot() as Call).evaluateFunction(varTable, function)
            }
            is Identifier -> return if (right is Link) VariableLink(
                right as Link,
                variable!!.getProperty(getAfterDot()) as Type
            ).evaluate(varTable) else variable!!.getProperty(getAfterDot())
        }
        throw PositionalException("Expected function or property", getAfterDot())
    }

    override fun isResolved(symbolTable: SymbolTable): Boolean {
        return when (right) {
            is Link -> (right as Link).isResolved(symbolTable)
            is Invocation -> true
            is Identifier -> {
                if (variable.hasProperty(right))
                    variable.getPropertyOrNull(right.value) != null
                else false
            }
            else -> throw PositionalException("Unexpected", right)
        }
    }

    override fun getFirstUnassigned(parent: Type): Assignment? {
        if (right is Link)
            return if (variable is Type)
                getFirstUnassigned(variable as Type)
            else getFirstUnassigned(parent)
    }
}
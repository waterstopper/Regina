package table

import lexer.PositionalException
import properties.Variable
import token.Token

class ScopeTable {
    private val variables: MutableMap<String, Variable> = mutableMapOf()

    fun addVariable(name: String, variable: Variable) {
        variables[name] = variable
    }

    fun getVariable(token: Token) = variables[token.value] ?: throw PositionalException("identifier not found", token)
    fun getVariable(name: String) = variables[name] ?: throw PositionalException("identifier not found")

    fun getVariableOrNull(name: String) = variables[name]

    companion object {
        /**
         * For function calls
         */
        fun fromArguments(arguments: List<Variable>, paramNames: List<String>): ScopeTable {
            val res = ScopeTable()
            for (index in arguments.indices)
                res.addVariable(paramNames[index], arguments[index])
            return res
        }
    }
}



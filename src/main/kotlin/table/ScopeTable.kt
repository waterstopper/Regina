package table

import lexer.NotFoundException
import properties.Variable
import token.Token

class ScopeTable {
    private val variables: MutableMap<String, Variable> = mutableMapOf()

    fun addVariable(name: String, variable: Variable) {
        variables[name] = variable
    }

    fun getVariable(token: Token) = variables[token.value] ?: throw NotFoundException(token)
    fun getVariable(name: String) = variables[name]
        ?: throw NotFoundException()

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

    fun copy(): ScopeTable {
        val res = ScopeTable()
        res.variables.putAll(variables.toMutableMap())
        return res
    }
}



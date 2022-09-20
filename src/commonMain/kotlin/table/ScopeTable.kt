package table

import properties.Variable

class ScopeTable {
    private val variables: MutableMap<String, Variable> = mutableMapOf()

    fun addVariable(name: String, variable: Variable) {
        variables[name] = variable
    }

    fun getVariables() = variables.toMutableMap()

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

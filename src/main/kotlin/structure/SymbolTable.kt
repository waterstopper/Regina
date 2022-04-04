package structure

import evaluation.FunctionEvaluation
import evaluation.TypeManager
import properties.Assignment
import properties.Function
import properties.Variable
import properties.Type

class SymbolTable(
    val variables: MutableMap<String, Variable> = mutableMapOf(),
    val functions: MutableMap<String, Function> = mutableMapOf()
) {
    companion object {
        fun getTypeTable(type: Type): SymbolTable {
            val res = SymbolTable(
                TypeManager.types as MutableMap<String, Variable>,
                FunctionEvaluation.functions
            )
            res.merge(SymbolTable(type.symbolTable.variables, type.symbolTable.functions))
            return res
        }
    }

    fun addVariables(variables: List<Variable>,names:List<String>) {
        for (i in variables.indices)
            this.variables[names[i]] = variables[i]
    }

    fun findFunction(name: String): Function? = functions[name]

    fun findIndentfier(name: String): Variable? = variables[name]

    fun copy(): SymbolTable = SymbolTable(variables.toMutableMap(), functions.toMutableMap())

    fun merge(symbolTable: SymbolTable): SymbolTable {
        return SymbolTable(
            variables.merge(symbolTable.variables),
            (functions as MutableMap<String, Variable>).merge(symbolTable.variables) as MutableMap<String, Function>
        )
    }

    private fun MutableMap<String, Variable>.merge(other: MutableMap<String, Variable>): MutableMap<String, Variable> {
        for (v in other)
            this[v.key] = v.value
        return this
    }

    override fun toString(): String =
        "variables:${variables}${if (functions.isNotEmpty()) "\nfunctions:$functions" else ""}"

    fun toStringWithAssignments(assignments: List<Assignment>): String {
        if (assignments.isEmpty())
            return this.toString()
        var res = "variables:${variables}$"
        res = res.substring(0, res.length - 2)
        res += "${assignments.joinToString(separator = ",")}}"

        return res
    }
}
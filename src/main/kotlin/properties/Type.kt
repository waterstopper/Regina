package properties

import structure.SymbolTable

class Type(
    name: String,
    val typeName: String,
    val type: Type?,
    parent: Type?,
    val assignments: MutableList<Assignment>,
    val exported: Any? = null
) :
    Property(name, parent) {
    val symbolTable = baseSymbolTable()
    //val properties: MutableMap<String,Property> = mutableMapOf()
    //val functions: MutableMap<String,Function> = mutableMapOf()

    override fun toString(): String {
        return "$typeName{name:${if (name == "") "-" else name}, parent:${parent ?: "-"}, ${
            symbolTable.toStringWithAssignments(
                assignments
            )
        }${if (exported != null) ",to $exported" else ""}}"
    }

    private fun baseSymbolTable(): SymbolTable {
        if (parent == null)
            return SymbolTable()
        val vars = mutableMapOf<String, Variable>()
        vars["parent"] = parent
        return SymbolTable(vars)
    }
}

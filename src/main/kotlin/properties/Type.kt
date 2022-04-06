package properties

import evaluation.ValueEvaluation
import lexer.PositionalException
import lexer.Token
import structure.SymbolTable

class Type(
    name: String,
    val typeName: String,
    private val type: Type?,
    parent: Type?,
    val assignments: MutableList<Assignment>,
    val exported: Any? = null
) :
    Property(name, parent), Cloneable {
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

    fun copy(): Type {
        val copy = Type("", typeName, this.type?.copy(), parent?.copy(), assignments.map { it.copy() }.toMutableList())
        copy.assignments.forEach { it.parent = copy }
        return copy
    }


    private fun baseSymbolTable(): SymbolTable {
        if (parent == null)
            return SymbolTable()
        val vars = mutableMapOf<String, Variable>()
        vars["parent"] = parent
        return SymbolTable(vars)
    }

    companion object {
        /**
         * similar to ValueEvaluation.evaluateLink()
         */
        fun getPropertyNameAndTable(token: Token, symbolTable: SymbolTable): Pair<String, SymbolTable> {
            var linkRoot = token
            var table = symbolTable
            while (linkRoot.value == ".") {
                val type = table.variables[linkRoot.left.value]
                if (type !is Type)
                    throw PositionalException("expected class", linkRoot.left)
                linkRoot = linkRoot.right
                table = type.symbolTable
            }
            return Pair(linkRoot.value, table)
        }
    }
}

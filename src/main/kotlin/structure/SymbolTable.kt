package structure

import lexer.FunctionEvaluation

class SymbolTable(val identifiers: MutableList<Node>, val functions: MutableList<Function>) {
    companion object {
        fun getTypeTable(type: Type): SymbolTable {
            val res = SymbolTable(TypeManager.types as MutableList<Node>, FunctionEvaluation.functions)
            res.merge(SymbolTable(type.properties, type.functions))
            return res
        }
    }

    fun findFunction(name: String): Function? = functions.find { it.name == name }

    fun findIndentfier(name: String): Node? = identifiers.find { it.name == name }

    fun copy(): SymbolTable = SymbolTable(identifiers.toMutableList(), functions.toMutableList())

    fun merge(symbolTable: SymbolTable): SymbolTable {
        return SymbolTable(
            identifiers.merge(symbolTable.identifiers),
            (functions as MutableList<Node>).merge(symbolTable.identifiers) as MutableList<Function>
        )
    }

    private fun MutableList<Node>.merge(other: MutableList<Node>): MutableList<Node> {
        val res = this.filter { e -> !other.any { it.name == e.name } }.toMutableList()
        res.addAll(other)
        return res
    }

}
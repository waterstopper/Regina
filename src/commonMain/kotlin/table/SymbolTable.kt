package table

import evaluation.FunctionFactory.initializeEmbedded
import lexer.PositionalException
import node.Node
import node.statement.Assignment
import properties.*
import utils.Utils.toVariable

class SymbolTable(
    // during recursive evaluation multiple symbol tables are used, hence need different scopes, files and types
    private var scopeTable: ScopeTable? = ScopeTable(),
    private var variableTable: Variable? = null,
    private var fileTable: FileTable,
    var resolvingType: Boolean
) {
    companion object { // import a.b.c as imported
        // imported.A() <- process
        private val imports = mutableMapOf<FileTable, MutableMap<String, FileTable>>()
        val globalFile = initializeGlobal()

        private fun initializeGlobal(): FileTable {
            val res = FileTable("@global", 0)
            for (i in initializeEmbedded())
                res.addFunction(i.value)
            return res
        }
    }

    fun getFileOfFunction(node: Node, function: RFunction): FileTable =
        fileTable.getFileOfFunction(node, function)

    fun getFileTable() = fileTable
    fun getScope() = scopeTable
    fun getCurrentType() = variableTable

    fun changeScope(): SymbolTable {
        return SymbolTable(
            variableTable = variableTable,
            fileTable = fileTable,
            resolvingType = resolvingType
        )
    }

    fun changeFile(fileTable: FileTable): SymbolTable {
        return SymbolTable(
            scopeTable?.copy(), variableTable,
            fileTable,
            resolvingType = resolvingType
        )
    }

    fun changeVariable(type: Variable) =
        SymbolTable(
            scopeTable?.copy(),
            type,
            if (type is Type) type.fileTable else fileTable,
            resolvingType = resolvingType
        )

    fun addVariable(name: String, value: Variable) = scopeTable!!.addVariable(name, value)

    fun getImportOrNull(importName: String) = fileTable.getImportOrNull(importName)
    fun getImportOrNullByFileName(fileName: String) =
        fileTable.getImportOrNullByFileName(fileName)

    fun getImport(node: Node) =
        fileTable.getImport(node)

    fun getType(node: Node): Type =
        fileTable.getTypeOrNull(node.value)
            ?: throw PositionalException(
                "Type `${node.value}` not found",
                node
            )

    fun getFunction(node: Node): RFunction {
        val res = fileTable.getFunctionOrNull(node)
        if (res == null) {
            if (variableTable == null)
                throw PositionalException("Function `${node.left.value}` not found", node)
            return variableTable!!.getFunction(node)
        }
        return res
    }

    fun getObjectOrNull(node: Node): Object? =
        fileTable.getObjectOrNull(node.value)

    fun getTypeOrNull(node: Node): Type? =
        fileTable.getTypeOrNull(node.value)

    fun getUncopiedTypeOrNull(node: Node): Type? = fileTable.getUncopiedType(node)

    fun getFunctionOrNull(node: Node): RFunction? = fileTable.getFunctionOrNull(node)
        ?: variableTable?.getFunctionOrNull(node)

    fun getVariableOrNull(name: String): Variable? = scopeTable?.getVariableOrNull(name)

    fun getIdentifier(node: Node): Variable = getIdentifierOrNull(node) ?: throw PositionalException(
        "Identifier `${node.value}` not found in `$fileTable`",
        node
    )

    fun getIdentifierOrNull(node: Node): Variable? {
        val variable = getVariableOrNull(node.value)
        if (variable != null)
            return variable
        val property = variableTable?.getPropertyOrNull(node.value)
        if (property != null)
            return property
        return getObjectOrNull(node)
    }

    fun getPropertyOrNull(name: String): Property? {
        return variableTable?.getPropertyOrNull(name)
    }

    fun getAssignmentOrNull(name: String): Assignment? {
        if (variableTable is Type) {
            return (variableTable as Type).getAssignment(name)
        }
        return null
    }

    fun copy() =
        SymbolTable(
            scopeTable?.copy() ?: ScopeTable(),
            variableTable,
            fileTable,
            resolvingType = resolvingType
        )

    fun addVariableOrNot(node: Node) = scopeTable?.addVariable(node.value, "".toVariable(node))

    override fun toString(): String {
        val res = StringBuilder()
        res.append(globalFile.stringNotation())
        for (i in imports.keys) {
            res.append("\n")
            res.append(i.stringNotation())
            if (imports[i]?.isNotEmpty() == true)
                res.append(
                    "\n\timports: ${
                        imports[i]!!.map { Pair(it.value, it.key) }.joinToString(separator = ",")
                    }\n"
                )
        }
        return res.toString()
    }

    fun getDictionaryFromTable(): Map<String, Any> {
        val res = mutableMapOf<Any, Any>()
        val maps = mutableMapOf<String, MutableMap<Any, Any>>()
        scopeTable?.getVariables()?.forEach { (name, variable) ->
            if (variable is Type)
                maps["instances"]!![name] = variable.toString()
            else maps["primitives"]!![name] = variable
        }
        val allInstances = mutableSetOf<Type>()
        for (instance in maps["instances"]!!.values) {
            allInstances.addAll((instance as Type).getAllInstances())
        }
        res["allInstances"] = allInstances
        res["scope"] = maps
        if (variableTable is Type)
            res["this"] = variableTable as Type
        return maps
    }

    fun toDebugString(): String {
        val res = StringBuilder()
        if (variableTable is Type) {
            res.append("instance fields:\n")
            for ((fieldName, fieldValue) in (variableTable as Type).getProperties().getPValue()) {
                res.append("\t$fieldName:$fieldValue\n")
            }
        }
        if (scopeTable != null)
            for ((fieldName, fieldValue) in scopeTable!!.getVariables())
                res.append("\t$fieldName:$fieldValue\n")
        if (res.isEmpty())
            return ""
        return res.deleteAt(res.lastIndex).toString()
    }
}

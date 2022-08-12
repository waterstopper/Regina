package table

import evaluation.FunctionFactory.initializeEmbedded
import lexer.PositionalException
import node.Node
import node.statement.Assignment
import properties.*
import properties.Function
import utils.Utils.toVariable

class SymbolTable(
    // during recursive evaluation multiple symbol tables are used, hence need different scopes, files and types
    private var scopeTable: ScopeTable? = ScopeTable(),
    private var variableTable: Variable? = null,
    private var fileTable: FileTable = FileTable(""),
    var resolvingType: Boolean
) {
    companion object { // import a.b.c as imported
        // imported.A() <- process
        private val imports = mutableMapOf<FileTable, MutableMap<String, FileTable>>()

        // private val embedded: MutableMap<String, Function> = initializeEmbedded()
        val globalFile = initializeGlobal()

        private fun initializeGlobal(): FileTable {
            val res = FileTable("@global")
            for (i in initializeEmbedded())
                res.addFunction(i.value)
            //imports[res] = mutableMapOf()
            return res
        }

//        fun initializeObjects() {
//            for ((file, _) in imports)
//                for (obj in file.getObjects())
//                    resolveTree(obj, globalTable)
//        }

//        fun clearTable() {
//            globalTable = SymbolTable()
//            imports.clear()
//            imports[globalFile] = mutableMapOf()
//        }
    }

//    private fun checkImports(check: (table: FileTable) -> Any?): List<Any> {
//        val suitable = mutableListOf<Any>()
//        for (table in imports[fileTable]!!.values) {
//            val fromFile = check(table)
//            if (fromFile != null)
//                suitable.add(fromFile)
//        }
//        return suitable
//    }

//    private fun getListFromFiles(getValue: (table: FileTable) -> Any?): List<Any> {
//        val fromCurrent = getValue(fileTable)
//        if (fromCurrent != null)
//            return listOf(fromCurrent)
//        return checkImports(getValue)
//    }

//    private fun getFromFilesOrNull(getValue: (table: FileTable) -> Any?): Any? {
//        val valuesList = getListFromFiles(getValue)
//        return if (valuesList.size == 1)
//            valuesList.first()
//        else null
//    }

//    private fun getFromFiles(node: Node, getValue: (table: FileTable) -> Any?): Any {
//        val valuesList = getListFromFiles(getValue)
//        if (valuesList.size >= 2)
//            throw PositionalException("Import ambiguity. `${node.value}` found in $valuesList", node)
//        if (valuesList.isEmpty())
//            throw PositionalException("`${node.value}` not found", node)
//        return valuesList.first()
//    }

    fun getFileOfFunction(node: Node, function: Function): FileTable =
        fileTable.getFileOfFunction(node, function)
//        val inCurrent = getValue(fileTable)
//        if (inCurrent != null)
//            return fileTable
//        val suitable = mutableListOf<FileTable>()
//        for (table in imports[fileTable]!!.values) {
//            val fromFile = getValue(table)
//            if (fromFile != null)
//                suitable.add(table)
//        }
//        return when (suitable.size) {
//            0 -> fileTable // if function is in class //throw PositionalException("File with `${token.value}` not found", token)
//            1 -> suitable.first()
//            else -> throw PositionalException("`${node.value}` is found in files: $suitable. Specify file.", node)
//        }
//    }

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

    fun addFile(fileName: String): Boolean {
        if (imports[FileTable(fileName)] == null) {
            imports[FileTable(fileName)] = mutableMapOf("@global" to globalFile)
            return true
        }
        return false
    }

    fun addImport(fileName: Node, importName: Node) {
        imports[fileTable]!![importName.value] = imports.keys.find { it.fileName == fileName.value }!!
    }

    fun addType(node: Node) = fileTable.addType(node)
    fun addFunction(function: Function) = fileTable.addFunction(function)
    fun addObject(node: Node) = fileTable.addObject(node)
    fun addVariable(name: String, value: Variable) = scopeTable!!.addVariable(name, value)

    fun getImportOrNull(importName: String) = fileTable.getImportOrNull(importName)//imports[fileTable]!![importName]
    fun getImportOrNullByFileName(fileName: String) =
        fileTable.getImportOrNullByFileName(fileName)//imports[fileTable]!!.values.find { it.fileName == fileName }

    fun getImport(node: Node) =
        fileTable.getImport(node)//imports[fileTable]!![node.value]
            ?: throw PositionalException("File not found", node)

    fun getType(node: Node): Type =
        fileTable.getTypeOrNull(node.value)//  getFromFiles(node) { it.getTypeOrNull(node.value) } as Type?
            ?: throw PositionalException(
                "Type `${node.value}` not found",
                node
            )

    fun getFunction(node: Node): Function {
        val res = fileTable.getFunctionOrNull(node)//getFromFilesOrNull { it.getFunctionOrNull(node) } as Function?
        if (res == null) {
            if (variableTable == null)
                throw PositionalException("Function `${node.left.value}` not found", node)
            return variableTable!!.getFunction(node)
        }
        return res
    }

    fun getObjectOrNull(node: Node): Object? =
        fileTable.getObjectOrNull(node.value)//getFromFilesOrNull { it.getObjectOrNull(node.value) } as Object?

    fun getTypeOrNull(node: Node): Type? =
        fileTable.getTypeOrNull(node.value)//getFromFilesOrNull { it.getTypeOrNull(node.value) } as Type?

    fun getFunctionOrNull(node: Node): Function? = fileTable.getFunctionOrNull(node)
        ?: variableTable?.getFunctionOrNull(node)
//        getFromFilesOrNull { it.getFunctionOrNull(node) } as Function?
//            ?: if (variableTable != null) variableTable!!.getFunctionOrNull(node) else null

//    fun getMain(): Function {
//        val mains = getFromFilesOrNull { it.getMain() }
//            ?: throw PositionalException("no main functions found")
//        return fileTable.getMain()
//    }

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
        return res.deleteCharAt(res.lastIndex).toString()
    }
}

package table

import evaluation.Evaluation.globalTable
import evaluation.FunctionFactory.initializeEmbedded
import lexer.PositionalException
import properties.*
import properties.Function
import token.Token
import token.statement.Assignment
import utils.Utils.toVariable

class SymbolTable(
    // during recursive evaluation multiple symbol tables are used, hence need different scopes, files and types
    private var scopeTable: ScopeTable? = ScopeTable(),
    private var variableTable: Variable? = null,
    private var fileTable: FileTable = FileTable("")
) {
    companion object { // import a.b.c as imported
        // imported.A() <- process
        private val imports = mutableMapOf<FileTable, MutableMap<String, FileTable>>()

        // private val embedded: MutableMap<String, Function> = initializeEmbedded()
        private val globalFile = initializeGlobal()

        private fun initializeGlobal(): FileTable {
            val res = FileTable("@global")
            for (i in initializeEmbedded())
                res.addFunction(i.value)
            imports[res] = mutableMapOf()
            return res
        }

//        fun initializeObjects() {
//            for ((file, _) in imports)
//                for (obj in file.getObjects())
//                    resolveTree(obj, globalTable)
//        }

        fun clearTable() {
            globalTable = SymbolTable()
            imports.clear()
            imports[globalFile] = mutableMapOf()
        }
    }

    private fun checkImports(check: (table: FileTable) -> Any?): List<Any> {
        val suitable = mutableListOf<Any>()
        for (table in imports[fileTable]!!.values) {
            val fromFile = check(table)
            if (fromFile != null)
                suitable.add(fromFile)
        }
        return suitable
    }

    private fun getListFromFiles(getValue: (table: FileTable) -> Any?): List<Any> {
        val fromCurrent = getValue(fileTable)
        if (fromCurrent != null)
            return listOf(fromCurrent)
        return checkImports(getValue)
    }

    private fun getFromFilesOrNull(getValue: (table: FileTable) -> Any?): Any? {
        val valuesList = getListFromFiles(getValue)
        return if (valuesList.size == 1)
            valuesList.first()
        else null
    }

    private fun getFromFiles(token: Token, getValue: (table: FileTable) -> Any?): Any {
        val valuesList = getListFromFiles(getValue)
        if (valuesList.size >= 2)
            throw PositionalException("Import ambiguity. `${token.value}` found in $valuesList", token)
        if (valuesList.isEmpty())
            throw PositionalException("`${token.value}` not found", token)
        return valuesList.first()
    }

    fun getFileOfValue(token: Token, getValue: (table: FileTable) -> Any?): FileTable {
        val inCurrent = getValue(fileTable)
        if (inCurrent != null)
            return fileTable
        val suitable = mutableListOf<FileTable>()
        for (table in imports[fileTable]!!.values) {
            val fromFile = getValue(table)
            if (fromFile != null)
                suitable.add(table)
        }
        when (suitable.size) {
            0 -> throw PositionalException("File with `${token.value}` not found", token)
            1 -> return suitable.first()
            else -> throw PositionalException("`${token.value}` is found in files: $suitable. Specify file.", token)
        }
    }

    fun getFileTable() = fileTable
    fun getScope() = scopeTable
    fun getCurrentType() = variableTable

    fun changeScope(): SymbolTable {
        return SymbolTable(variableTable = variableTable, fileTable = fileTable)
    }

    fun changeScope(scopeTable: ScopeTable?): SymbolTable =
        SymbolTable(scopeTable = scopeTable?.copy(), variableTable = variableTable, fileTable = fileTable)

    fun changeFile(fileTable: FileTable): SymbolTable {
        return SymbolTable(
            scopeTable?.copy(), variableTable,
            imports.keys.find { it == fileTable }
                ?: throw PositionalException("File not found")
        )
    }

    fun changeFile(fileName: String): SymbolTable {
        return SymbolTable(
            scopeTable?.copy(), variableTable,
            imports.keys.find { it.fileName == fileName }
                ?: throw PositionalException("File not found")
        )
    }

    fun changeVariable(type: Variable) =
        SymbolTable(scopeTable?.copy(), type, if (type is Type) changeFile(type.fileName).fileTable else fileTable)

    fun addFile(fileName: String): Boolean {
        if (imports[FileTable(fileName)] == null) {
            imports[FileTable(fileName)] = mutableMapOf("@global" to globalFile)
            return true
        }
        return false
    }

    fun addImport(fileName: Token, importName: Token) {
        imports[fileTable]!![importName.value] = imports.keys.find { it.fileName == fileName.value }!!
    }

    fun addType(token: Token) = fileTable.addType(token)
    fun addFunction(function: Function) = fileTable.addFunction(function)
    fun addObject(token: Token) = fileTable.addObject(token)
    fun addVariable(name: String, value: Variable) = scopeTable!!.addVariable(name, value)

    fun getImportOrNull(importName: String) = imports[fileTable]!![importName]
    fun getImportOrNullByFileName(fileName: String) = imports[fileTable]!!.values.find { it.fileName == fileName }
    fun getFileFromType(type: Type, token: Token) = imports.keys.find { it == type.fileName }
        ?: throw PositionalException("File `${type.fileName.fileName}` not found", token)

    fun getImport(token: Token) =
        imports[fileTable]!![token.value]
            ?: throw PositionalException("File not found", token)

    fun getType(token: Token): Type =
        getFromFiles(token) { it.getTypeOrNull(token.value) } as Type?
            ?: throw PositionalException(
                "Type `${token.value}` not found",
                token
            )

    fun getFunction(token: Token): Function {
        val res = getFromFilesOrNull { it.getFunctionOrNull(token) } as Function?
        if (res == null) {
            if (variableTable == null) throw PositionalException("Function `${token.value}` not found", token)
            return variableTable!!.getFunction(token)
        }
        return res
    }

    fun getObjectOrNull(token: Token): Object? = getFromFilesOrNull { it.getObjectOrNull(token.value) } as Object?
    fun getTypeOrNull(token: Token): Type? = getFromFilesOrNull { it.getTypeOrNull(token.value) } as Type?

    fun getFunctionOrNull(token: Token): Function? =
        getFromFilesOrNull { it.getFunctionOrNull(token) } as Function?
            ?: if (variableTable != null) variableTable!!.getFunctionOrNull(token) else null

    fun getMain(): Function {
        val mains = getFromFilesOrNull { it.getMain() }
            ?: throw PositionalException("no main functions found")
        return fileTable.getMain()
    }

    fun getVariableOrNull(name: String): Variable? = scopeTable?.getVariableOrNull(name)

    fun getIdentifier(token: Token): Variable = getIdentifierOrNull(token) ?: throw PositionalException(
        "Identifier `${token.value}` not found in `$fileTable`",
        token
    )

    fun getIdentifierOrNull(token: Token): Variable? {
        val variable = getVariableOrNull(token.value)
        if (variable != null)
            return variable
        val property = variableTable?.getPropertyOrNull(token.value)
        if (property != null)
            return property
        return getObjectOrNull(token)
    }

    fun getTypes(): MutableMap<String, MutableMap<String, Type>> {
        val res = mutableMapOf<String, MutableMap<String, Type>>()
        for (i in imports.keys)
            res[i.fileName] = i.getTypes()
        return res
    }

    fun getProperty(token: Token): Property = variableTable!!.getProperty(token)

    fun getPropertyOrNull(name: String): Property? {
        return variableTable?.getPropertyOrNull(name)
    }

    fun getAssignmentOrNull(name: String): Assignment? {
        if (variableTable is Type) {
            return (variableTable as Type).getAssignment(name)
        }
        return null
    }

    fun copy() = SymbolTable(scopeTable?.copy() ?: ScopeTable(), variableTable, fileTable)
    fun addVariableOrNot(token: Token) = scopeTable?.addVariable(token.value, "".toVariable(token))

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

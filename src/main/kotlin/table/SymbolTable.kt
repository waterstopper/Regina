package table

import evaluation.FunctionEvaluation.initializeEmbedded
import evaluation.FunctionEvaluation.toVariable
import lexer.PositionalException
import properties.*
import properties.Function
import token.Token

class SymbolTable(
    // during recursive evaluation multiple symbol tables are used, hence need different scopes, files and types
    private var scopeTable: ScopeTable? = ScopeTable(),
    private var typeTable: Type? = null,
    private var fileTable: FileTable = FileTable("")
) {
    companion object {
        private val imports = mutableMapOf<FileTable, MutableSet<FileTable>>()

        //private val embedded: MutableMap<String, Function> = initializeEmbedded()
        private val globalFile = initializeGlobal()

        private fun initializeGlobal(): FileTable {
            val res = FileTable("@global")
            for (i in initializeEmbedded())
                res.addFunction(i.value)
            imports[res] = mutableSetOf()
            return res
        }

        fun getEmbeddedNames(): MutableSet<String> = globalFile.getFunctionNames()
    }

    private fun checkImports(check: (table: FileTable) -> Any?): List<Any> {
        val suitable = mutableListOf<Any>()
        for (table in imports[fileTable]!!) {
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
            throw PositionalException("Import ambiguity. ${token.value} found in $valuesList", token)
        if (valuesList.isEmpty())
            throw PositionalException("${token.value} not found", token)
        return valuesList.first()
    }

    fun getFileOfValue(token: Token, getValue: (table: FileTable) -> Any?): FileTable {
        val inCurrent = getValue(fileTable)
        if (inCurrent != null)
            return fileTable
        val suitable = mutableListOf<FileTable>()
        for (table in imports[fileTable]!!) {
            val fromFile = getValue(table)
            if (fromFile != null)
                suitable.add(table)
        }
        when (suitable.size) {
            0 -> throw PositionalException("File with ${token.value} not found", token)
            1 -> return suitable.first()
            else -> throw PositionalException("${token.value} is found in files: $suitable. Specify file.", token)
        }
    }

    fun changeScope(): SymbolTable {
        return SymbolTable(typeTable = typeTable, fileTable = fileTable)
    }

    fun changeFile(fileName: String): SymbolTable {
        return SymbolTable(scopeTable?.copy(), typeTable, imports.keys.find { it.fileName == fileName }
            ?: throw PositionalException("File not found"))
    }

    // TODO dangerous if type is in different file
    fun changeType(type: Type) = SymbolTable(scopeTable?.copy(), type, fileTable)

    fun addFile(fileName: String): Boolean {
        if (imports[FileTable(fileName)] == null) {
            imports[FileTable(fileName)] = mutableSetOf(globalFile)
            return true
        }
        return false
    }

    fun addImport(fileName: Token) {
        imports[fileTable]!!.add(imports.keys.find { it.fileName == fileName.value }!!)
    }

    fun addType(token: Token) = fileTable.addType(token)
    fun addFunction(function: Function) = fileTable.addFunction(function)
    fun addObject(token: Token) = fileTable.addObject(token)
    fun addVariable(name: String, value: Variable) = scopeTable!!.addVariable(name, value)


    fun getImportOrNull(fileName: String) = imports[fileTable]!!.find { it.fileName == fileName }
    fun getType(token: Token): Type =
        getFromFiles(token) { it.getTypeOrNull(token.value)!!.copy() } as Type? ?: throw PositionalException(
            "Type ${token.value} not found",
            token
        )

    fun getFunction(token: Token): Function {
        val res = getFromFilesOrNull { it.getFunctionOrNull(token.value) } as Function?
        if (res == null) {
            if (typeTable == null) throw PositionalException("Function ${token.value} not found", token)
            return typeTable!!.getFunction(token)
        }
        return res
    }

    fun getObjectOrNull(token: Token): Object? = getFromFilesOrNull { it.getObjectOrNull(token.value) } as Object?
    fun getTypeOrNull(token: Token): Type? = getFromFilesOrNull { it.getTypeOrNull(token.value) } as Type?
    fun getFunctionOrNull(token: Token): Function? =
        getFromFilesOrNull { it.getFunctionOrNull(token.value) } as Function?

    fun getMain(): Function {
        val mains = getFromFilesOrNull { it.getFunctionOrNull("main") }
            ?: throw PositionalException("no main functions found")
        return fileTable.getFunctionOrNull("main") ?: throw PositionalException("no main function in current file")
    }

    fun getVariable(token: Token) = scopeTable!!.getVariable(token)
    fun getVariable(name: String) = scopeTable!!.getVariable(name)
    fun getVariableOrNull(name: String): Variable? = scopeTable!!.getVariableOrNull(name)
    fun getIdentifier(token: Token): Any {
        val variable = scopeTable?.getVariableOrNull(token.value)
        if (variable != null)
            return variable
        val type = getTypeOrNull(token)
        if (type != null)
            return type
        val property = typeTable?.getPropertyOrNull(token.value)
        if (property != null)
            return property
        return getObjectOrNull(token) ?: throw PositionalException(
            "Identifier ${token.value} not found in $fileTable",
            token
        )
    }

    fun getCurrentType() = typeTable

    fun getTypes(): List<Type> {
        val res = mutableListOf<Type>()
        for (i in imports.keys)
            res.addAll(i.getTypes())
        return res
    }

    fun getProperty(token: Token): Property {
        return typeTable!!.getProperty(token)
    }

    fun copy() = SymbolTable(scopeTable?.copy() ?: ScopeTable(), typeTable, fileTable)
    fun addVariableOrNot(token: Token) = scopeTable?.addVariable(token.value, "".toVariable(token, null))

    override fun toString(): String {
        val res = StringBuilder()
        res.append(globalFile.stringNotation())
        for (i in imports.keys) {
            res.append("\n")
            res.append(i.stringNotation())
            if (imports[i]?.isNotEmpty() == true)
                res.append("\n\timports: ${imports[i]!!.joinToString(separator = ",")}\n")
        }
        return res.toString()
    }
}
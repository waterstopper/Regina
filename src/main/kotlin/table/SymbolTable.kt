package table

import evaluation.FunctionEvaluation.initializeEmbedded
import lexer.PositionalException
import properties.*
import properties.Function
import token.Token

typealias importType = MutableMap<String, MutableMap<String, Invokable>>

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

    fun getFromFilesOrNull(getValue: (table: FileTable) -> Any?): Any? {
        val valuesList = getListFromFiles(getValue)
        return if (valuesList.size == 1)
            valuesList.first()
        else null
    }

    fun getFromFiles(token: Token, getValue: (table: FileTable) -> Any?): Any {
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
        return SymbolTable(fileTable = imports.keys.find { it.fileName == fileName }
            ?: throw PositionalException("File not found"))
    }

    // TODO dangerous if type is in different file
    fun changeType(type: Type) = SymbolTable(scopeTable, type, fileTable)

    fun addFile(fileName: String) {
        imports[FileTable(fileName)] = mutableSetOf(globalFile)
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
        return getObjectOrNull(token) ?: throw PositionalException("Identfier ${token.value} not found", token)
    }

    fun getCurrentType() = typeTable

    private fun addGlobal() {

    }

    fun getTypes(): List<Type> {
        val res = mutableListOf<Type>()
        for (i in imports.keys)
            res.addAll(i.getTypes())
        return res
    }

}


//        return if (findFunction(token.left.value) != null)
//            FunctionEvaluation.evaluateFunction(
//                token, findFunction(token.left.value)!!,
//                token.children.subList(1, token.children.size), this
//            )
//        else {
//            // bad for properties inside class. Need to create some global variable in TypeEvaluation
//            // if it is true then resolve tree, else just add type-property to existing instance
//            return if (TypeEvaluation.resolving) TypeManager.types[token.left.value]!!.copy()
//            else TypeEvaluation.resolveTree(TypeManager.types[token.left.value]!!.copy())
//        }


//    fun getVariable(token: Token, name: String, fileName: String = ""): Variable {
//        if (variables[name] == null && variables[name]!!.isEmpty())
//            throw PositionalException("", token)
//        if (fileName == "")
//            if (variables[name]!!.size == 1)
//                return variables[name]!![0]
//            else throw PositionalException("ambiguity", token)
//        else{
//            return variables[name]!!.find { it. }
//        }
//    }


//    fun toStringWithAssignments(assignments: List<TokenAssignment>): String {
//        if (assignments.isEmpty())
//            return ""
//        var res = "variables:${variables}$"
//        res = res.substring(0, res.length - 2)
//        res += "${assignments.joinToString(separator = ",")}}"
//
//        return res
//    }

//    fun getInvokableOrNull(token: Token): Invokable? {
//        val res = try {
//            getType(token)
//        } catch (e: PositionalException) {
//            try {
//                getFunction(token)
//            } catch (e: PositionalException) {
//                return null
//            }
//        }
//        return res
//    }


//    fun getDeclared(nameToken: Token, map: MutableMap<String, MutableMap<String, Invokable>>): Invokable {
////        if (nameToken.value == ".") {
//////            if (nameToken.left.value == currentFile)
//////                return map[nameToken.right.value]?.get(currentFile) ?: throw PositionalException(
//////                    "identifier not found",
//////                    nameToken.right
//////                )
////            if (nameToken.right.value == "(") {
////                return map[nameToken.right.left.value]?.get(nameToken.left.value) ?: throw PositionalException(
////                    "identifier not found",
////                    nameToken.right
////                )
////            }
////            return map[nameToken.right.value]?.get(nameToken.left.value) ?: throw PositionalException(
////                "identifier not found",
////                nameToken.right
////            )
////        }
//        val declarations =
//            map[nameToken.value] ?: throw PositionalException(
//                "identifier ${nameToken.value} not found in $currentFile",
//                nameToken
//            )
//        val filtered =
//            declarations.filter { importMap[currentFile]?.contains(it.key) ?: false || it.key == currentFile }
//        if (filtered.isEmpty())
//            throw PositionalException("identifier ${nameToken.value} not found in $currentFile", nameToken)
//        if (filtered.size > 1)
//            throw PositionalException("import ambiguity. Such identifier found in ${filtered.keys}")
//        return filtered.values.first()
//    }
//
//    private fun getDeclaredOrNull(
//        nameToken: Token,
//        map: MutableMap<String, MutableMap<String, Invokable>>
//    ): Invokable? {
//        val declarations = map[nameToken.value] ?: return null
//        if (declarations[currentFile] != null)
//            return declarations[currentFile]
//        val filtered =
//            declarations.filter { importMap[currentFile]?.contains(it.key) ?: false }
//        if (filtered.size != 1)
//            return null
//        else return filtered.values.first()
//    }
//
//    // TODO rewrite bullshit code
//    fun getType(name: String): Type? = types[name]?.get(currentFile)
//
//    fun getTypeOrNull(token: Token): Type? =
//        getDeclaredOrNull(token, types as importType) as Type?
//
//    fun getFunction(token: Token): Function {
//        return embedded[token.value] ?: getDeclared(
//            token,
//            functions as importType
//        ) as Function
//    }

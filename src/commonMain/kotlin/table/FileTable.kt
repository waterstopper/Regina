package table

import evaluation.FunctionFactory
import lexer.ExpectedTypeException
import lexer.NotFoundException
import lexer.PositionalException
import node.Identifier
import node.ImportNode
import node.Meta
import node.Node
import node.invocation.Call
import node.statement.Assignment
import properties.Object
import properties.RFunction
import properties.Type
import table.SymbolTable.Companion.globalFile

// @Serializable
class FileTable(
    val filePath: String,
    val index: Int
) {
    private val types: MutableSet<Type> = mutableSetOf()
    private val objects: MutableSet<Object> = mutableSetOf()
    private val functions: MutableSet<RFunction> = mutableSetOf()
    private val imports: MutableMap<String, FileTable> = mutableMapOf()
    var numberInstances: Int = 0

    init {
        imports["Global"] = globalFile
    }

    fun addType(node: Node): Type {
        val name = node.left.value

        val (assignments, functions) = createAssignmentsAndFunctions(node.children[2])
        // copied types get index 0
        val added = Type(name, assignments, this, 0)
        added.functions.addAll(functions)
        if (types.find { it.name == name } != null)
            throw PositionalException("Two classes with same name in `$filePath`", filePath, node)
        types.add(added)
        for (assignment in added.assignments)
            assignment.parent = added
        return added
    }

    fun addObject(node: Node) {
        if (node.left !is Identifier) {
            throw PositionalException("Object cannot be inherited", filePath, node)
        }
        val name = node.left.value
        val (assignments, functions) = createAssignmentsAndFunctions(node.right)
        if (objects.find { it.name == name } != null) {
            throw PositionalException("Two objects with same name", filePath, node)
        }
        objects.add(Object(name, assignments, this))
        objects.last().functions.addAll(functions)
    }

    fun addFunction(function: RFunction) {
        val res = functions.add(function)
        if (!res) {
            throw PositionalException(
                "Two functions with same signature (name and number of non-default parameters) `$function` in $filePath",
                filePath
            )
        }
    }

    fun addImport(importNode: ImportNode, fileTable: FileTable) {
        if (imports.filter { it.value == fileTable }.isNotEmpty()) {
            throw PositionalException("Same import found above", filePath, importNode)
        }
        imports[importNode.importName] = fileTable
    }

    fun getTypeOrNull(name: String): Type? = getFromFilesOrNull {
        it.types.find { type -> type.name == name }?.copyRoot()
    } as Type?

    fun getType(node: Node): Type = getTypeOrNull(node.value)
        ?: throw NotFoundException(node, filePath)

    fun getUncopiedType(node: Node): Type? = types.find { it.name == node.value }

    fun getObjectOrNull(name: String): Object? = getFromFilesOrNull {
        it.objects.find { obj -> obj.name == name }
    } as Object?

    fun getFunctionOrNull(node: Node): RFunction? {
        return getFromFilesOrNull { fileTable ->
            RFunction.getFunctionOrNull(node as Call, fileTable.functions)
        } as RFunction?
    }

    fun getFunction(node: Node): RFunction =
        getFunctionOrNull(node) ?: throw PositionalException("Function not found in `$filePath`", filePath, node)

    fun getImportOrNull(importName: String) = imports[importName]
    fun getImportOrNullByFileName(fileName: String) = imports.values.find { it.filePath == fileName }
    fun getImport(node: Node) = imports[node.value] ?: throw PositionalException("File not found", filePath, node)

    fun getMain(): RFunction {
        val mains = functions.filter { it.name == "main" }
        if (mains.isEmpty()) {
            throw PositionalException("main not found in `$filePath`", filePath)
        }
        if (mains.size > 1) {
            throw PositionalException("Found 2 or more main functions in `$filePath`", filePath)
        }
        return mains.first()
    }

    private fun createAssignmentsAndFunctions(node: Node): Pair<MutableSet<Assignment>, List<RFunction>> {
        val res = mutableSetOf<Assignment>()
        val functions = mutableListOf<RFunction>()
        for (a in node.children) {
            if (a is Assignment) {
                if (a.right is Assignment) {
                    throw PositionalException("Double assignments are not allowed in class", filePath, a)
                }
                if (!res.add(a)) {
                    throw PositionalException("Same property found above", filePath, a)
                }
                a.isProperty = true
            } else if (a.symbol == "fun") {
                functions.add(
                    FunctionFactory.createFunction(a, this)
                )
            } else if (a is Meta) {
                // skip sendMessage(Message("breakpoint", a.position.first))
            } else throw ExpectedTypeException(listOf(Assignment::class, RFunction::class), filePath, a)
        }

        return Pair(res, functions)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileTable) return false
        if (filePath != other.filePath) return false
        return true
    }

    override fun hashCode(): Int = filePath.hashCode()

    override fun toString(): String = filePath
    fun stringNotation(): String {
        val res = StringBuilder(filePath)
        if (types.isNotEmpty()) {
            res.append("\n")
        }
        for (type in types)
            res.append("\t$type\n")
        if (functions.isNotEmpty()) {
            res.append("\n")
        }
        for (func in functions)
            res.append("\t$func\n")
        return res.toString()
    }

    fun getTypes(): MutableMap<String, Type> = types.associateBy { it.name }.toMutableMap()
    fun getObjects() = objects
    fun getFunctions() = functions
    fun getFileOfFunction(node: Node, function: RFunction): FileTable {
        val inCurrent = functions.find { it == function }
        if (inCurrent != null) {
            return this
        }
        val suitable = mutableListOf<FileTable>()
        for (table in imports.values) {
            val fromFile = table.functions.find { it == function }
            if (fromFile != null) {
                suitable.add(table)
            }
        }
        return when (suitable.size) {
            0 -> this // if function is in class //throw PositionalException("File with `${token.value}` not found", token)
            1 -> suitable.first()
            else -> throw PositionalException("`$node` is found in files: $suitable. Specify file.", filePath, node)
        }
    }

    private fun getFromFilesOrNull(getValue: (table: FileTable) -> Any?): Any? {
        val valuesList = getListFromFiles(getValue)
        return if (valuesList.size == 1) {
            valuesList.first()
        } else null
    }

    private fun getListFromFiles(getValue: (table: FileTable) -> Any?): List<Any> {
        val fromCurrent = getValue(this)
        if (fromCurrent != null) {
            return listOf(fromCurrent)
        }
        return checkImports(getValue)
    }

    private fun checkImports(check: (table: FileTable) -> Any?): List<Any> {
        val suitable = mutableListOf<Any>()
        for (fileTable in imports.values) {
            val fromFile = check(fileTable)
            if (fromFile != null) {
                suitable.add(fromFile)
            }
        }
        return suitable
    }
}

package table

import delete.Delete
import evaluation.FunctionFactory
import lexer.ExpectedTypeException
import lexer.NotFoundException
import lexer.PositionalException
import lexer.RuntimeError
import node.Identifier
import node.Node
import node.invocation.Call
import node.statement.Assignment
import properties.Function
import properties.Object
import properties.Type

class FileTable(
    val fileName: String
) {
    private val types: MutableSet<Type> = mutableSetOf()
    private val objects: MutableSet<Object> = mutableSetOf()
    private val functions: MutableSet<Function> = mutableSetOf()
    private val imports: MutableMap<String, FileTable> = mutableMapOf()

    fun addType(node: Node) {
        val name = node.left.value

        val (assignments, functions) = createAssignmentsAndFunctions(node.children[2])
        val added = Type(name, null, assignments, this)
        added.functions.addAll(functions)
        if (types.find { it.name == name } != null)
            throw PositionalException("Two classes with same name in `$fileName`", node)
        types.add(added)
        for (assignment in added.assignments)
            assignment.parent = added
    }

    fun addObject(node: Node) {
        if (node.left !is Identifier)
            throw PositionalException("Object cannot be inherited", node)
        val name = node.left.value
        val (assignments, functions) = createAssignmentsAndFunctions(node.right)
        if (objects.find { it.name == name } != null)
            throw PositionalException("Two objects with same name", node)
        objects.add(Object(name, assignments, this))
        objects.last().functions.addAll(functions)
    }

    fun addFunction(function: Function) {
        val res = functions.add(function)
        if (!res)
            throw PositionalException("Two functions with same signature (name and number of non-default parameters) `$function` in $fileName")
    }

    fun getTypeOrNull(name: String): Type? = types.find { it.name == name }?.copy()
    fun getType(node: Node): Type = types.find { it.name == node.value }?.copy()
        ?: throw NotFoundException(node)

    fun getUncopiedType(node: Node): Type = types.find { it.name == node.value }
        ?: throw throw NotFoundException(node)

    fun getObjectOrNull(name: String) = objects.find { it.name == name }

    fun getFunction(node: Node): Function =
        getFunctionOrNull(node) ?: throw PositionalException("Function not found in `$fileName`", node)

    fun getFunctionOrNull(node: Node): Function? = Function.getFunctionOrNull(node as Call, functions)

    fun getMain(): Function {
        val mains = functions.filter { it.name == "main" }
        if (mains.isEmpty())
            throw PositionalException("main not found in `$fileName`")
        if (mains.size > 1)
            throw PositionalException("Found 2 or more main functions in `$fileName`")
        return mains.first()
    }

    private fun createAssignmentsAndFunctions(node: Node): Pair<MutableSet<Assignment>, List<Function>> {
        val res = mutableSetOf<Assignment>()
        val functions = mutableListOf<Function>()
        for (a in node.children) {
            if (a is Assignment) {
                if (!res.add(a))
                    throw PositionalException("Same property found above", a)
                a.isProperty = true
            } else if (a.symbol == "fun")
                functions.add(
                    FunctionFactory.createFunction(a)
                )
            else throw ExpectedTypeException(listOf(Assignment::class, Function::class), a)
        }

        return Pair(res, functions)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileTable) return false
        if (fileName != other.fileName) return false
        return true
    }

    override fun hashCode(): Int = fileName.hashCode()

    override fun toString(): String = fileName
    fun stringNotation(): String {
        val res = StringBuilder(fileName)
        if (types.isNotEmpty())
            res.append("\n")
        for (type in types)
            res.append("\t$type\n")
        if (functions.isNotEmpty())
            res.append("\n")
        for (func in functions)
            res.append("\t$func\n")
        return res.toString()
    }

    fun getTypes(): MutableMap<String, Type> = types.associateBy { it.name }.toMutableMap()
    fun getObjects() = objects
    fun getFileOfValue(delete: Delete, getValue: (table: FileTable) -> Any?): FileTable {
        val inCurrent = getValue(this)
        if (inCurrent != null)
            return this
        val suitable = mutableListOf<FileTable>()
        for (table in imports.values) {
            val fromFile = getValue(table)
            if (fromFile != null)
                suitable.add(table)
        }
        return when (suitable.size) {
            0 -> this // if function is in class //throw PositionalException("File with `${token.value}` not found", token)
            1 -> suitable.first()
            else -> throw RuntimeError("`${delete}` is found in files: $suitable. Specify file.", delete)
        }
    }
}

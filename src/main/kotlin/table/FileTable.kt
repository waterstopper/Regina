package table

import evaluation.FunctionFactory
import lexer.ExpectedTypeException
import lexer.NotFoundException
import lexer.PositionalException
import properties.Function
import properties.Object
import properties.Type
import token.Token
import token.invocation.Call
import token.statement.Assignment

class FileTable(
    val fileName: String
) {
    private val types: MutableSet<Type> = mutableSetOf()
    private val objects: MutableSet<Object> = mutableSetOf()
    private val functions: MutableSet<Function> = mutableSetOf()

    fun addType(token: Token) {
        //assignName(assignType(assignExported(token.left), fileName))
        val name = token.left.value
        val exported = if (token.children[2].value != "") token.children[2].value else null

        val (assignments, functions) = createAssignmentsAndFunctions(token.children[3])
        val added = Type(name, null, assignments, this, exported)
        added.functions.addAll(functions)
        if (types.find { it.name == name } != null)
            throw PositionalException("Two classes with same name in `$fileName`", token)
        val res = types.add(added)
        for (assignment in added.assignments)
            assignment.parent = added
    }

    fun addObject(token: Token) {
        val name = token.left.value
        val (assignments, functions) = createAssignmentsAndFunctions(token.right)
        if (objects.find { it.name == name } != null)
            throw PositionalException("Two objects with same name", token)
        objects.add(Object(name, assignments, this))
        objects.last().functions.addAll(functions)
    }

    fun addFunction(function: Function) {
        val res = functions.add(function)
        if (!res)
            throw PositionalException("Two functions with same signature (name and number of non-default parameters) `$function` in $fileName")
    }

    fun getTypeOrNull(name: String): Type? = types.find { it.name == name }?.copy()
    fun getType(token: Token): Type = types.find { it.name == token.value }?.copy()
        ?: throw NotFoundException(token)

    fun getUncopiedType(token: Token): Type = types.find { it.name == token.value }
        ?: throw throw NotFoundException(token)


    fun getObjectOrNull(name: String) = objects.find { it.name == name }

    fun getFunction(token: Token): Function =
        getFunctionOrNull(token) ?: throw PositionalException("Function not found in `$fileName`", token)

    fun getFunctionOrNull(token: Token): Function? = Function.getFunctionOrNull(token as Call, functions)

    fun getMain(): Function {
        val mains = functions.filter { it.name == "main" }
        if (mains.isEmpty())
            throw PositionalException("main not found in `$fileName`")
        if (mains.size > 1)
            throw PositionalException("Found 2 or more main functions in `$fileName`")
        return mains.first()
    }

    private fun createAssignmentsAndFunctions(token: Token): Pair<MutableList<Assignment>, List<Function>> {
        val res = mutableListOf<Assignment>()
        val functions = mutableListOf<Function>()
        for (a in token.children) {
            if (a is Assignment) {
                res.add(a)
                a.isProperty = true
            }
            else if (a.symbol == "fun")
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
}
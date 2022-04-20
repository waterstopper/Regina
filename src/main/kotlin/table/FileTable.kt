package table

import lexer.PositionalException
import properties.Function
import properties.Object
import properties.Type
import token.Token
import token.statement.TokenAssignment

class FileTable(
    val fileName: String
) {
    private val types: MutableSet<Type> = mutableSetOf()
    private val objects: MutableSet<Object> = mutableSetOf()
    private val functions: MutableSet<Function> = mutableSetOf()

    companion object {
        var exported: Any? = null
        private var superType: Type? = null
        private var superTypes = mutableMapOf<Pair<String, String>, Token>()
        var name = ""
    }

    fun addType(token: Token) {
        assignName(assignType(assignExported(token.left), fileName))
        val (assignments, functions) = createAssignmentsAndFunctions(token)
        val added = Type(name, null, assignments, fileName, exported)
        if (types.find { it.name == name } != null)
            throw PositionalException("found class with same name in $fileName", token)
        types.add(added)
        for (assignment in added.assignments)
            assignment.parent = added
    }

    fun addObject(token: Token) {
        val name = token.left.value
        val (assignments, functions) = createAssignmentsAndFunctions(token)
        objects.add(Object(name, assignments, fileName))
    }

    fun addFunction(function: Function) = functions.add(function)

    fun getTypeOrNull(name: String) = types.find { it.name == name }
    fun getObjectOrNull(name: String) = objects.find { it.name == name }
    fun getFunctionOrNull(name: String) = functions.find { it.name == name }
    fun getFunctionNames() = functions.map { it.name }.toMutableSet()

    fun assignExported(token: Token): Token {
        if (token.value == "export") {
            exported = token.right.value
            return token.left
        } else
            exported = null
        return token
    }

    fun assignType(token: Token, fileName: String): Token {
        if (token.value == ":") {
            superTypes[Pair(token.left.value, fileName)] = token.right
            return token.left
        } else
            superType = null
        return token
    }

    fun assignName(token: Token) {
        name = token.value
    }

    private fun createAssignmentsAndFunctions(token: Token): Pair<MutableList<TokenAssignment>, List<Token>> {
        val res = mutableListOf<TokenAssignment>()
        val functions = mutableListOf<Token>()
        for (a in token.right.children) {
            if (a is TokenAssignment)
                res.add(a)
            else if (a.symbol == "fun")
                functions.add(a)
            else throw PositionalException("expected assignment or function", a)
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
}
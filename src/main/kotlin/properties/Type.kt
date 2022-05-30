package properties

import lexer.PositionalException
import properties.primitive.PDictionary
import properties.primitive.PInt
import table.SymbolTable
import token.Token
import token.TokenFactory
import token.invocation.Constructor
import token.statement.Assignment
import java.util.*

open class Type(
    val name: String,
    parent: Type?,
    val assignments: MutableList<Assignment>,
    val fileName: String,
    private val exported: Any? = null,
    private val exportArgs: Any? = null,
    var supertype: Type? = null
) :
    Property(parent), Invokable {
    private val properties = mutableMapOf<String, Property>()
    val functions = mutableListOf<Function>()

    fun getAssignment(token: Token): Assignment? = assignments.find { it.left == token }
    fun removeAssignment(assignment: Assignment) = assignments.remove(assignment)
    fun removeAssignment(token: Token): Assignment? {
        for (a in assignments)
            if (a.left == token) {
                assignments.remove(a)
                return a
            }
        return null
    }

    override fun getFunctionOrNull(name: String): Function? = functions.find { it.name == name }
    override fun getFunction(token: Token) = functions.find { it.name == token.value }
        ?: throw PositionalException("\"$name\" class does not contain `${token.value}` function", token)

    override fun hasProperty(token: Token): Boolean {
        return when (token.value) {
            "parent" -> getParentOrNull() is Type
            else -> properties[token.value] != null
        }
    }

    override fun getProperties() = PDictionary(properties, this)

    override fun getPropertyOrNull(name: String) = when (name) {
        "parent" -> getParentOrNull()
        "properties" -> getProperties()
        else -> properties[name]
    }

    override fun getProperty(token: Token) = when (token.value) {
        "parent" -> getParentOrNull()
        "properties" -> getProperties()
        else -> properties[token.value] ?: PInt(
            0,
            this
        )
    }

    fun setProperty(name: String, value: Property) {
        properties[name] = value
    }

    override fun toString(): String {
        val res = StringBuilder(name)
//        if (supertype != null) {
//            res.append(":")
//            if (supertype!!.fileName != fileName)
//                res.append("${supertype!!.fileName}.")
//            res.append(supertype!!.name)
//        }
//        if (exported != null)
//            res.append("->$exported")
//        res.append("{parent:${parent?.name ?: "-"}, ${properties.filter { it.key != "parent" }}, $assignments}")
        return res.toString()

    }

    fun inherits(other: Type): Boolean {
        var type: Type? = this
        while (type != null) {
            if (type.name == other.name && type.fileName == other.fileName)
                return true
            type = type.supertype
        }
        return false
    }

    fun copy(): Type {
        val copy =
            Type(
                name = name,
                parent = parent?.copy(),
                assignments = assignments.map { TokenFactory().copy(it) as Assignment }.toMutableList(),
                fileName = fileName,
                exported = exported,
                exportArgs = exportArgs,
                supertype = supertype
            )
        copy.assignments.forEach { it.parent = copy }
        copy.functions.addAll(this.functions)
        return copy
    }

    companion object{
        var resolving = false

        fun resolveTree(root: Type, symbolTable: SymbolTable): Type {
            root.setProperty("parent", PInt(0, root))
            resolving = true
            do {
                val (current, parent) = bfs(root) ?: break
                val stack = Stack<Assignment>()
                stack.add(current)
                processAssignment(parent, symbolTable.changeVariable(parent), stack)
            } while (true)
            resolving = false
            return root
        }

        fun processAssignment(parent: Type, symbolTable: SymbolTable, stack: Stack<Assignment>) {
            while (stack.isNotEmpty()) {
                val unresolved = stack.pop()
                val top = unresolved.getFirstUnassigned(symbolTable, parent)
                if (top != null)
                    stack.add(top)
                else unresolved.assign(parent, symbolTable.changeVariable(parent))
            }
        }

        /**
         * Find unresolved assignments
         */
        fun bfs(root: Type): Pair<Assignment, Type>? {
            val stack = Stack<Type>()
            val visited = Stack<Type>()
            stack.add(root)
            while (stack.isNotEmpty()) {
                val current = stack.pop()
                visited.add(current)
                if (current.assignments.isNotEmpty())
                    return Pair(current.assignments.first(), current)
                val containers = current.getProperties().getPValue().values.filterIsInstance<Type>()
                stack.addAll(containers.filter { !visited.contains(it) })
            }
            return null
        }
    }
}
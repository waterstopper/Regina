package properties

import lexer.PositionalException
import properties.primitive.PDictionary
import properties.primitive.PInt
import table.FileTable
import table.SymbolTable
import token.Link
import token.Token
import token.TokenFactory
import token.invocation.Call
import token.statement.Assignment
import utils.Utils.toVariable

/**
 * Is a class. In documentation might be referred as class or type interchangeably.
 *
 * Classes are mutable, meaning assigning same instance to different variables `a` and b` will change `a` if `b` is changed.
 */
open class Type(
    val name: String,
    parent: Type?,
    val assignments: MutableSet<Assignment>,
    val fileName: FileTable,
    private val exported: Any? = null,
    private val exportArgs: Any? = null,
    var supertype: Type? = null
) :
    Property(parent) {
    protected val properties = mutableMapOf<String, Property>()
    val functions = mutableSetOf<Function>()

    fun getAssignment(token: Token): Assignment? = assignments.find { it.left == token }
    fun getLinkedAssignment(link: Link, index: Int): Assignment? {
        val identProperty = getAssignment(link.children[index])
        if (identProperty != null)
            return identProperty
        for (childrenNumber in 2..link.children.size - index) {
            val searched = TokenFactory.copy(link, index, childrenNumber)
            val found = getAssignment(searched)
            if (found != null)
                return found
        }
        return null
    }

    fun removeAssignment(assignment: Assignment) = assignments.remove(assignment)
    fun removeAssignment(token: Token): Assignment? {
        for (a in assignments)
            if (a.left == token) {
                assignments.remove(a)
                return a
            }
        return null
    }

    private fun getInheritedFunctions(): Set<Function> {
        if (supertype != null)
            return supertype!!.functions + supertype!!.getInheritedFunctions()
        return setOf()
    }

    private fun getInheritedAssignments(): Set<Assignment> {
        if (supertype != null)
            return supertype!!.assignments + supertype!!.getInheritedAssignments()
        return setOf()
    }

    override fun getFunctionOrNull(token: Token): Function? =
        Function.getFunctionOrNull(token as Call, functions + getInheritedFunctions())

    override fun getFunction(token: Token) = getFunctionOrNull(token)
        ?: throw PositionalException("Class `$name` does not contain function", token)

    override fun getProperties() =
        PDictionary(properties.mapKeys { (key, _) -> key.toVariable() }.toMutableMap(), this)

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
            if (type.equalToType(other))
                return true
            type = type.supertype
        }
        return false
    }

    fun equalToType(other: Type): Boolean {
        return name == other.name && fileName == other.fileName && other !is Object
    }

    fun copy(): Type {
        val copy =
            Type(
                name = name,
                parent = parent?.copy(),
                assignments = (assignments + getInheritedAssignments()).map { TokenFactory.copy(it) as Assignment }
                    .toMutableSet(),
                fileName = fileName,
                exported = exported,
                exportArgs = exportArgs,
                supertype = supertype
            )
        copy.assignments.forEach { it.parent = this }
        copy.functions.addAll(this.functions)
        return copy
    }

    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int {
        return super.hashCode()
    }

    companion object {
        var resolving = false

        fun resolveTree(root: Type, symbolTable: SymbolTable): Type {
            root.setProperty("parent", PInt(0, root))
            resolving = true
            do {
                val (current, parent) = bfs(root) ?: break
                val stack = mutableListOf<Pair<Type, Assignment>>()
                stack.add(Pair(parent, current))
                processAssignment(symbolTable.changeVariable(parent), stack)
            } while (true)
            resolving = false
            return root
        }

        fun processAssignment(symbolTable: SymbolTable, stack: MutableList<Pair<Type, Assignment>>) {
            // here type should be part of stack
            while (stack.isNotEmpty()) {
                val unresolved = stack.removeLast()
                val top = unresolved.second.getFirstUnassigned(
                    symbolTable.changeVariable(unresolved.first), unresolved.first)
                if (top.second != null) {
                    //     if (trainingWheels && (stack + unresolved).contains(top))
                    //        throw PositionalException("Assignment encountered recursively during initialization of $parent", top)
                    stack.add(top as Pair<Type, Assignment>)
                } else unresolved.second.assign(unresolved.first, symbolTable.changeVariable(unresolved.first))
            }
        }

        /**
         * Find unresolved assignments
         */
        private fun bfs(root: Type): Pair<Assignment, Type>? {
            val stack = mutableListOf<Type>()
            val visited = mutableListOf<Type>()
            stack.add(root)
            while (stack.isNotEmpty()) {
                val current = stack.removeLast()
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

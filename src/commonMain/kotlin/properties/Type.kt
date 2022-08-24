package properties

import DebugType
import NestableDebug
import References
import elementToDebug
import lexer.PositionalException
import node.Link
import node.Node
import node.TokenFactory
import node.invocation.Call
import node.statement.Assignment
import properties.primitive.PDictionary
import properties.primitive.PInt
import properties.primitive.Primitive
import table.FileTable
import table.SymbolTable
import utils.Utils.toVariable

/**
 * Is a class. In documentation might be referred as class or type interchangeably.
 *
 * Classes are mutable, meaning assigning same instance to different variables `a` and b` will change `a` if `b` is changed.
 */
//@Serializable
open class Type(
    val name: String,
    parent: Type?,
    val assignments: MutableSet<Assignment>,
    val fileTable: FileTable,
    val index: Int,
    var supertype: Type? = null
) :
    Property(parent), NestableDebug {
    protected val properties = mutableMapOf<String, Property>()
    val functions = mutableSetOf<RFunction>()

    fun getAssignment(node: Node): Assignment? = assignments.find { it.left == node }
    fun getAssignment(name: String): Assignment? = assignments.find { it.left.value == name }
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
    fun removeAssignment(assignment: delete.Assignment) {//assignments.remove(assignment) TODO
    }

    fun removeAssignment(node: Node): Assignment? {
        for (a in assignments)
            if (a.left == node) {
                assignments.remove(a)
                return a
            }
        return null
    }

    fun getAllInstances(): Set<Type> {
        val res = mutableSetOf(this)
        properties.values.forEach {
            if (it is Type)
                res.addAll(it.getAllInstances())
        }
        return res
    }

    private fun getInheritedFunctions(): Set<RFunction> {
        if (supertype != null)
            return supertype!!.functions + supertype!!.getInheritedFunctions()
        return setOf()
    }

    private fun getInheritedAssignments(): Set<Assignment> {
        if (supertype != null)
            return supertype!!.assignments + supertype!!.getInheritedAssignments()
        return setOf()
    }

    override fun getFunctionOrNull(node: Node): RFunction? =
        RFunction.getFunctionOrNull(node as Call, functions + getInheritedFunctions())

    override fun getFunction(node: Node) = getFunctionOrNull(node)
        ?: throw PositionalException("Class `$name` does not contain function", node)

    override fun getProperties() =
        PDictionary(
            properties.mapKeys { (key, _) -> key.toVariable() }.toMutableMap(),
            this,
            Primitive.dictionaryId++
        )

    override fun toDebugClass(references: References): Any {
        val id = getDebugId()
        references.queue.remove(id)
        if (references.types[id.second] != null)
            return id
        val res = DebugType(properties.map {
            it.key to if (it.value == this) id else elementToDebug(it.value, references)
        }.toMap().toMutableMap())
        references.types[id.second as String] = res
        return id
    }

    override fun getDebugId(): Pair<String, Any> = Pair("type", toString())

    override fun getPropertyOrNull(name: String) = when (name) {
        "parent" -> getParentOrNull()
        "properties" -> getProperties()
        else -> properties[name]
    }

    override fun getProperty(node: Node) = when (node.value) {
        "parent" -> getParentOrNull()
        "properties" -> getProperties()
        else -> properties[node.value] ?: PInt(
            0,
            this
        )
    }

    fun setProperty(name: String, value: Property) {
        properties[name] = value
    }

    override fun toString(): String {
        if (index == 0)
            return name
        if (fileTable.fileName.isEmpty())
            throw Exception("Empty fileTable name")
        val fileLetter = if (fileTable.fileName.contains("/"))
            fileTable.fileName.split("/").last().first()
        else fileTable.fileName.first()
        val res = StringBuilder("$name-$fileLetter${fileTable.index}$index")
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
        return name == other.name && fileTable == other.fileTable && other !is Object
    }

    fun copy(): Type {
        fileTable.numberInstances += 1
        val index = fileTable.numberInstances
        val copy =
            Type(
                name = name,
                parent = parent?.copy(),
                assignments = (assignments + getInheritedAssignments()).map { TokenFactory.copy(it) as Assignment }
                    .toMutableSet(),
                fileTable = fileTable,
                supertype = supertype,
                index = index
            )
        copy.assignments.forEach { it.parent = this }
        copy.functions.addAll(this.functions)
        return copy
    }

    override fun equals(other: Any?): Boolean = this === other
// StackOverFlow on js test if it's uncommented
//    override fun hashCode(): Int {
//        println("Hash")
//        return super.hashCode()
//    }

    companion object {
        fun resolveTree(root: Type, symbolTable: SymbolTable): Type {
            root.setProperty("parent", PInt(0, root))
            symbolTable.resolvingType = true
            do {
                val (current, parent) = bfs(root) ?: break
                val stack = mutableListOf<Pair<Type, Assignment>>()
                stack.add(Pair(parent, current))
                processAssignment(symbolTable.changeVariable(parent), stack)
            } while (true)
            symbolTable.resolvingType = false
            return root
        }

        fun processAssignment(symbolTable: SymbolTable, stack: MutableList<Pair<Type, Assignment>>) {
            // here type should be part of stack
            while (stack.isNotEmpty()) {
                val unresolved = stack.removeLast()
                val top = unresolved.second.getFirstUnassigned(
                    symbolTable.changeVariable(unresolved.first), unresolved.first
                )
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

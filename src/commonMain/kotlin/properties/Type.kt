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
import node.invocation.Invocation
import node.invocation.ResolvingMode
import node.statement.Assignment
import properties.primitive.PDictionary
import properties.primitive.Primitive
import table.FileTable
import table.SymbolTable
import utils.Utils
import utils.Utils.NULL
import utils.Utils.toVariable

/**
 * Is a class. In documentation might be referred as class or type interchangeably.
 *
 * Classes are mutable, meaning assigning same instance to different variables `a` and b` will change `a` if `b` is changed.
 */
// @Serializable
open class Type(
    val name: String,
    val assignments: MutableSet<Assignment>,
    val fileTable: FileTable,
    val index: Int,
    var supertype: Type? = null,
    protected val properties: MutableMap<String, Property> = mutableMapOf()
) : Property(), NestableDebug {

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
            if (it is Type) {
                res.addAll(it.getAllInstances())
            }
        }
        return res
    }

    private fun getInheritedFunctions(): Set<RFunction> {
        if (supertype != null) {
            return supertype!!.functions + supertype!!.getInheritedFunctions()
        }
        return setOf()
    }

    private fun getInheritedAssignments(): Set<Assignment> {
        if (supertype != null) {
            return supertype!!.assignments + supertype!!.getInheritedAssignments()
        }
        return setOf()
    }

    override fun getFunctionOrNull(node: Node): RFunction? =
        RFunction.getFunctionOrNull(node as Call, functions + getInheritedFunctions())

    override fun getFunction(node: Node, fileTable: FileTable) = getFunctionOrNull(node)
        ?: throw PositionalException("Class `$name` does not contain function", fileTable.filePath, node)

    override fun getProperties() =
        PDictionary(
            properties.mapKeys { (key, _) -> key.toVariable() }.toMutableMap(),
            Primitive.dictionaryId++
        )

    override fun toDebugClass(references: References, copying: Boolean): Pair<String, Any> {
        val id = getDebugId()
        references.queue.remove(id)
        if (references.types[id.second] != null)
            return id
        val copy: Type?
        if (copying) {
            fileTable.numberInstances += 1
            val index = fileTable.numberInstances
            copy =
                Type(
                    name = name,
                    assignments = (assignments + getInheritedAssignments()).map { TokenFactory.copy(it) as Assignment }
                        .toMutableSet(),
                    fileTable = fileTable,
                    supertype = supertype,
                    index = index
                )
            copy.functions.addAll(functions)
        } else copy = null
        if (this is Object)
            resolveAllProperties()
        val res = DebugType(
            properties.map {
                it.key to if (it.value == this) id else elementToDebug(it.value, references)
            }.toMap().toMutableMap(), copy
        )
        references.types[id.second as String] = res
        return id
    }

    override fun getDebugId(): Pair<String, Any> = Pair("Type", toString())

    override fun getPropertyOrNull(name: String) = when (name) {
        "properties" -> getProperties()
        else -> properties[name]
    }

    override fun getProperty(node: Node, fileTable: FileTable) = when (node.value) {
        "properties" -> getProperties()
        else -> properties[node.value] ?: throw PositionalException("Property not found", fileTable.filePath, node)
    }

    fun setProperty(name: String, value: Property) {
        properties[name] = value
    }

    override fun toString(): String {
        if (index == 0) {
            return name
        }
        if (fileTable.filePath.isEmpty()) {
            throw Exception("Empty fileTable name")
        }
        val fileLetter = if (fileTable.filePath.contains("/")) {
            fileTable.filePath.split("/").last().first()
        } else fileTable.filePath.first()
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
            if (type.equalToType(other)) {
                return true
            }
            type = type.supertype
        }
        return false
    }

    fun equalToType(other: Type): Boolean {
        return name == other.name && fileTable == other.fileTable && other !is Object
    }

    fun copyRoot(): Type {
        fileTable.numberInstances += 1
        val index = fileTable.numberInstances
        val copy =
            Type(
                name = name,
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
    fun callAfter(symbolTable: SymbolTable, all: Boolean = false) {
        val afterNode = Call(Utils.parseOneNode(if (all) "afterAll()" else "after()") as Invocation)
        val afterResolving = getFunctionOrNull(afterNode)
        if (afterResolving != null)
            afterNode.evaluateFunction(symbolTable, afterResolving)
    }

    companion object {
        fun resolveTree(root: Type, symbolTable: SymbolTable): Type {
            root.setProperty("parent", NULL)
            symbolTable.resolvingType = ResolvingMode.TYPE
            val visitedTypes = mutableSetOf<String>()
            do {
                val (parent, current) = bfs(root, visitedTypes) ?: break
                val stack = mutableListOf<Pair<Type, Assignment>>()
                stack.add(Pair(parent, current))
                processAssignment(symbolTable.changeVariable(parent), stack, visitedTypes)
            } while (true)
            symbolTable.resolvingType = ResolvingMode.FUNCTION
            afterAllBfs(mutableSetOf(), mutableListOf(), symbolTable)
            return root
        }

        fun processAssignment(
            symbolTable: SymbolTable,
            stack: MutableList<Pair<Type, Assignment>>,
            visitedTypes: MutableSet<String>
        ) {
            while (stack.isNotEmpty()) {
                val unresolved = stack.removeLast()
                val top = unresolved.second.getFirstUnassigned(
                    symbolTable.changeVariable(unresolved.first),
                    unresolved.first
                )
                if (top.second != null) {
                    stack.add(top as Pair<Type, Assignment>)
                } else {
                    unresolved.second.assign(unresolved.first, symbolTable.changeVariable(unresolved.first))
                    val newProperty = unresolved.first.properties[unresolved.second.name]
                    // resolve container property before moving further
                    if (newProperty is Containerable) {
                        val containerStack = mutableListOf<Containerable>()
                        containerStack.add(newProperty)
                        while (containerStack.isNotEmpty()) {
                            val visitedContainers = mutableSetOf<Int>()
                            val assignment = containerBfs(containerStack, visitedContainers, visitedTypes)
                            if (assignment != null) {
                                stack.add(assignment)
                                processAssignment(symbolTable, stack, visitedTypes)
                            }
                        }
                    }
                    if (unresolved.first.assignments.isEmpty())
                        unresolved.first.callAfter(symbolTable)
                }
            }
        }

        private fun containerBfs(
            containerStack: MutableList<Containerable>,
            visited: MutableSet<Int>,
            visitedTypes: MutableSet<String>
        ): Pair<Type, Assignment>? {
            for (i in containerStack.last().getCollection()) {
                when (i) {
                    is Type -> {
                        if (i.toString() !in visitedTypes) {
                            val res = bfs(i, visitedTypes)
                            if (res != null) return res
                        }
                    }
                    is Containerable -> {
                        if (i.getContainerId() !in visited) {
                            containerStack.add(i)
                            val res = containerBfs(containerStack, visited, visitedTypes)
                            if (res != null) return res
                        }
                    }
                }
            }
            visited.add(containerStack.removeLast().getContainerId())
            return null
        }

        /**
         * Find unresolved assignments in type instances.
         */
        private fun bfs(root: Type, visited: MutableSet<String>): Pair<Type, Assignment>? {
            val stack = mutableListOf<Type>()
            val currentlyVisited = mutableSetOf<String>()
            stack.add(root)
            while (stack.isNotEmpty()) {
                val current = stack.removeLast()
                if (current.assignments.isNotEmpty())
                    return Pair(current, current.assignments.first())

                val currentId = current.toString()
                if (currentId !in visited && currentId !in currentlyVisited) {
                    currentlyVisited.add(currentId)
                    val properties = current.properties.values.filterIsInstance<Type>()
                    stack.addAll(
                        properties.filter {
                            val id = it.toString()
                            !visited.contains(id) && !currentlyVisited.contains(id)
                        }
                    )
                }
            }
            visited.addAll(currentlyVisited)
            return null
        }

        private fun afterAllBfs(visited: MutableSet<Any>, stack: MutableList<Any>, symbolTable: SymbolTable) {
            val condition = { it: Variable ->
                it is Containerable && it.getContainerId() !in visited
                        || it is Type && it.toString() !in visited
            }
            while (stack.isNotEmpty()) {
                val current = stack.removeLast()
                if (current is Type && current.toString() !in visited) {
                    visited.add(current)
                    current.callAfter(symbolTable, all = true)
                    stack.addAll(current.properties.values.filter(condition))
                } else if (current is Containerable && current.getContainerId() !in visited) {
                    visited.add(current)
                    stack.addAll(current.getCollection().filter(condition))
                }
            }
        }
    }
}

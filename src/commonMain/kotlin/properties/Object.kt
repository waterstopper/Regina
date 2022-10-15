package properties

import lexer.PositionalException
import node.Node
import node.invocation.ResolvingMode
import node.statement.Assignment
import table.FileTable
import table.SymbolTable
/**
 * Object is a [singleton][https://en.wikipedia.org/wiki/Singleton_pattern] Type
 */
class Object(name: String, assignments: MutableSet<Assignment>, fileTable: FileTable) :
    Type(name, assignments, fileTable, 0) {
    override fun getProperty(node: Node, fileTable: FileTable): Property {
        if (properties[node.value] != null) {
            return properties[node.value]!!
        }
        val assignment = assignments.find { it.left.value == node.value }
        if (assignment != null) {
            processAssignment(
                SymbolTable(fileTable = fileTable, resolvingType = ResolvingMode.OBJECT),
                mutableListOf(Pair(this, assignment)),
                mutableSetOf()
            )
            return properties[node.value]!!
        }
        throw PositionalException("Property not found", fileTable.filePath, node)
    }

    override fun toString(): String {
        return "$name-Object"
    }

    override fun getDebugId(): Pair<String, Any> = Pair("Object", toString())

    override fun getPropertyOrNull(name: String): Property? {
        if (properties[name] != null) {
            return properties[name]!!
        }
        val assignment = assignments.find { it.left.value == name }
        if (assignment != null) { // TODO is resolvingType really false? What if it happens inside type property and object property has type as property?
            processAssignment(
                SymbolTable(fileTable = fileTable, resolvingType = ResolvingMode.OBJECT),
                mutableListOf(Pair(this, assignment)),
                mutableSetOf()
            )
            return properties[name]!!
        }
        return null
    }

    fun resolveAllProperties() {
        for(a in assignments)
            getPropertyOrNull(a.name)
    }

    override fun getFunction(node: Node, fileTable: FileTable) = getFunctionOrNull(node)
        ?: throw PositionalException("Object `$name` does not contain function", fileTable.filePath, node)

    override fun equals(other: Any?) = this === other
}


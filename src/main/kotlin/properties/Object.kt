package properties

import lexer.PositionalException
import properties.primitive.PInt
import table.FileTable
import table.SymbolTable
import node.Node
import node.statement.Assignment

/**
 * Object is a [singleton][https://en.wikipedia.org/wiki/Singleton_pattern] Type
 */
class Object(name: String, assignments: MutableSet<Assignment>, fileTable: FileTable) :
    Type(name, null, assignments, fileTable) {
    override fun getProperty(node: Node): Property {
        if (properties[node.value] != null)
            return properties[node.value]!!
        val assignment = assignments.find { it.left.value == node.value }
        if (assignment != null) {
            processAssignment(SymbolTable(fileTable = fileTable), mutableListOf(Pair(this, assignment)))
            return properties[node.value]!!
        }
        return PInt(0, null)
    }

    override fun getPropertyOrNull(name: String): Property? {
        if (properties[name] != null)
            return properties[name]!!
        val assignment = assignments.find { it.left.value == name }
        if (assignment != null) {
            processAssignment(SymbolTable(fileTable = fileTable), mutableListOf(Pair(this, assignment)))
            return properties[name]!!
        }
        return null
    }

    override fun getFunction(node: Node) = getFunctionOrNull(node)
        ?: throw PositionalException("Object `$name` does not contain function", node)

    override fun equals(other: Any?) = this === other
}

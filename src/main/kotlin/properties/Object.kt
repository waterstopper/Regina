package properties

import lexer.PositionalException
import properties.primitive.PInt
import table.FileTable
import table.SymbolTable
import token.Token
import token.statement.Assignment

/**
 * Object is a [singleton][https://en.wikipedia.org/wiki/Singleton_pattern] Type
 */
class Object(name: String, assignments: MutableList<Assignment>, fileName: FileTable) :
    Type(name, null, assignments, fileName) {
    override fun getProperty(token: Token): Property {
        if (properties[token.value] != null)
            return properties[token.value]!!
        val assignment = assignments.find { it.left.value == token.value }
        if (assignment != null) {
            processAssignment(this, SymbolTable(fileTable = fileName), mutableListOf(assignment))
            return properties[token.value]!!
        }
        return PInt(0, null)
    }

    override fun getPropertyOrNull(name: String): Property? {
        if (properties[name] != null)
            return properties[name]!!
        val assignment = assignments.find { it.left.value == name }
        if (assignment != null) {
            processAssignment(this, SymbolTable(fileTable = fileName), mutableListOf(assignment))
            return properties[name]!!
        }
        return PInt(0, null)
    }

    override fun getFunction(token: Token) = getFunctionOrNull(token)
        ?: throw PositionalException("Object `$name` does not contain function", token)

    override fun equals(other: Any?) = this === other
}

package node

import lexer.NotFoundException
import lexer.PositionalException
import node.statement.Assignment
import properties.Object
import properties.Type
import properties.primitive.PNumber
import properties.primitive.Primitive
import table.SymbolTable
import utils.Utils.toProperty
import utils.Utils.toVariable

open class Identifier(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
) : Node(symbol, value, position), Assignable, Linkable {
    constructor(value: String) : this(value, value, position = Pair(0, 0))

    override fun evaluate(symbolTable: SymbolTable): Any {
        val variable = symbolTable.getIdentifierOrNull(this)
            ?: return symbolTable.getUncopiedTypeOrNull(this) ?: throw NotFoundException(
                this,
                symbolTable.getFileTable().filePath
            )
        if (variable is Primitive && variable !is PNumber)
            return (variable).getPValue()
        return variable
    }

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any) {
        if (parent != null && assignment.isProperty) {
            parent.setProperty(this.value, value.toProperty(assignment.right, parent))
            val property = parent.getProperty(this,symbolTable.getFileTable())
            if (property is Type) {
                if (property.index == 0 && property !is Object)
                    throw PositionalException(
                        "Cannot assign class reference as a property. Use instance instead",
                        symbolTable.getFileTable().filePath,
                        this
                    )
                property.parent = parent
                property.setProperty("parent", parent)
            }
        }
        symbolTable.addVariable(this.value, value.toVariable(this))
    }

    override fun getFirstUnassigned(parent: Type, symbolTable: SymbolTable): Pair<Type, Assignment?> {
        return Pair(parent, parent.getAssignment(this))
    }

    override fun getPropertyName(): Node = this
}

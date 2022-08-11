package node

import lexer.NotFoundException
import node.statement.Assignment
import properties.Type
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
            ?: return symbolTable.getTypeOrNull(this) ?: throw NotFoundException(
                this,
                file = symbolTable.getFileTable()
            )
        if (variable is Primitive)
            return (variable).getPValue()
        return variable
    }

    override fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any) {
        if (parent != null && assignment.isProperty) {
            parent.setProperty(this.value, value.toProperty(assignment.right, parent))
            if (parent.getProperty(this) is Type) {
                (parent.getProperty(this) as Type).parent = parent
                (parent.getProperty(this) as Type).setProperty("parent", parent)
            }
        }
        symbolTable.addVariable(this.value, value.toVariable(this))
    }

    override fun getFirstUnassigned(parent: Type, symbolTable: SymbolTable): Pair<Type, Assignment?> {
        return Pair(parent, parent.getAssignment(this))
    }

    override fun getPropertyName(): Node = this
}

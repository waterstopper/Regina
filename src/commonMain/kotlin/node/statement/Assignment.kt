package node.statement

import lexer.PositionalException
import node.Assignable
import node.Link
import node.Node
import node.operator.Operator
import properties.Type
import table.SymbolTable

class Assignment(
    symbol: String = "",
    value: String,
    position: Pair<Int, Int> = Pair(0, 0),
    children: MutableList<Node> = mutableListOf()
) : Operator(symbol, value, position, children) {
    init {
        this.children.clear()
        this.children.addAll(children)
        if (left !is Assignable) {
            throw PositionalException("Left operand is not assignable", "", left) // TODO filepath is empty
        }
    }

    var parent: Type? = null
    val name: String get() = left.value
    var isProperty = false

    override fun evaluate(symbolTable: SymbolTable): Any {
        val value = right.evaluate(symbolTable)
        (left as Assignable).assign(
            this,
            if (symbolTable.getCurrentType() is Type) (symbolTable.getCurrentType() as Type) else null,
            symbolTable,
            value
        )
        return value
    }

    fun getAssignable(): Assignable = left as Assignable

    /**
     * Find first unassigned property.
     * Lvalue of assignment is [Link], check that all link properties (except last one) are assigned
     * TODO check link in class. Should not work because all props are required
     */
    fun getFirstUnassigned(symbolTable: SymbolTable, parent: Type): Pair<Type, Assignment?> {
        if (left is Link) {
            val leftUnassigned = (left as Link).getFirstUnassignedOrNull(parent, symbolTable, forLValue = true)
            if (leftUnassigned.second != null)
                return leftUnassigned as Pair<Type, Assignment?>
        }
        return right.findUnassigned(symbolTable.changeVariable(parent), parent) ?: Pair(
            parent,
            null
        ) // right.traverseUnresolvedOptional(symbolTable.changeVariable(parent), parent)
    }

    fun assign(parent: Type, symbolTable: SymbolTable) {
        parent.removeAssignment(this)
        (left as Assignable).assign(this, parent, symbolTable, right.evaluate(symbolTable))
    }

    /**
     * To automatically replace assignments in type with constructor arguments
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Assignment) {
            return false
        }
        return left == other.left
    }

    override fun hashCode(): Int {
        return left.hashCode()
    }
}

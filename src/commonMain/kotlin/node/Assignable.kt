package node

import node.statement.Assignment
import properties.Type
import table.SymbolTable

/**
 * Marks tokens that can be on the left side of assignment.
 * Consequently, they can be properties and variables (hence, the methods)
 *
 * @property type TODO probably should add type checker property to all assignables, Like: `variable: Int = 0`.
 * It should throw exception when rValue is not Int
 */
interface Assignable {
    // var type: Any - type checker
    fun assign(assignment: Assignment, parent: Type?, symbolTable: SymbolTable, value: Any)

    /**
     * Used for dynamic instantiation. Finds first unassigned variable in token
     * (it is applicable mostly for [Link], because [Identifier] and [Index][node.operator.Index] can be either assigned or not,
     * while links can be assigned partially:
     *
     * `a.b.c.d` - here instance `a` can have property b of type [Type],
     * which does not have property `c` yet.)
     */
    fun getFirstUnassigned(parent: Type, symbolTable: SymbolTable): Pair<Type, Assignment?>
    fun getPropertyName(): Node
}

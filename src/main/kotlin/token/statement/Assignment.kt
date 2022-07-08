package token.statement

import lexer.Parser
import properties.Type
import table.SymbolTable
import token.Assignable
import token.Link
import token.Token
import token.operator.Operator

class Assignment(
    symbol: String = "",
    value: String,
    position: Pair<Int, Int> = Pair(0, 0),
    bindingPower: Int = 0,
    nud: ((token: Token, parser: Parser) -> Token)? = null,
    led: ((token: Token, parser: Parser, token2: Token) -> Token)? = null,
    std: ((token: Token, parser: Parser) -> Token)? = null,
    children: MutableList<Token> = mutableListOf()
) : Operator(symbol, value, position, bindingPower, nud, led, std) {
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    var parent: Type? = null
    val name: String get() = left.value
    var isProperty = false

    override fun evaluate(symbolTable: SymbolTable): Any {
        val value = right.evaluate(symbolTable)
        (left as Assignable).assign(
            this,
            if (symbolTable.getCurrentType() is Type) (symbolTable.getCurrentType() as Type) else null,
            symbolTable, value
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
        return right.traverseUnresolvedOptional(symbolTable, parent)
    }

    fun assign(parent: Type, symbolTable: SymbolTable) {
        parent.removeAssignment(this)
        (left as Assignable).assign(this, parent, symbolTable, right.evaluate(symbolTable))
    }

    /**
     * To automatically replace assignments in type with constructor arguments
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Assignment)
            return false
        return left == other.left
    }

    override fun hashCode(): Int {
        return left.hashCode()
    }
}

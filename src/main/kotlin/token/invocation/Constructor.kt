package token.invocation

import lexer.Parser
import properties.Type
import table.SymbolTable
import token.Identifier
import token.Token
import token.statement.Assignment
import java.util.*

class Constructor(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?, children: List<Token>
) : Identifier(symbol, value, position, bindingPower, nud, led, std) {
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    val name: Token
        get() = left

    override fun evaluate(symbolTable: SymbolTable): Any {
        val type = symbolTable.getType(left)
        return evaluateType(type, symbolTable)
    }

    fun evaluateType(type: Type, symbolTable: SymbolTable): Any {
        return if (resolving) type else resolveTree(type, symbolTable)
    }

    private fun resolveTree(root: Type, symbolTable: SymbolTable): Type {
        resolving = true
        do {
            val (current, parent) = bfs(root) ?: break
            val stack = Stack<Assignment>()
            stack.add(current)
            processAssignment(parent, symbolTable, stack)
        } while (true)
        resolving = false
        return root
    }

    private fun processAssignment(parent: Type, symbolTable: SymbolTable, stack: Stack<Assignment>) {
        while (stack.isNotEmpty()) {
            val top = stack.pop().getFirstUnassigned(parent)
            if (top != null)
                top.assign(parent, symbolTable)
            else stack.add(top)
        }
    }

    /**
     * Find unresolved assignments
     */
    private fun bfs(root: Type): Pair<Assignment, Type>? {
        val stack = Stack<Type>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val current = stack.pop()
            if (current.assignments.isNotEmpty())
                return Pair(current.assignments.first(), current)
            val containers = current.getProperties().values.filterIsInstance<Type>()
            stack.addAll(containers)
        }
        return null
    }

    companion object {
        var resolving = false
    }
}
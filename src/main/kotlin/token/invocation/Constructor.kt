package token.invocation

import properties.Type
import table.SymbolTable
import token.Token
import token.statement.Assignment
import java.util.*

// TODO why derived from Identifier
class Constructor(
    token: Token
) : Invocation(
    token.symbol, token.value,
    token.position, token.bindingPower,
    token.nud, token.led, token.std,
    token.children
) {
    init {
        this.children.clear()
        this.children.addAll(token.children)
    }

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
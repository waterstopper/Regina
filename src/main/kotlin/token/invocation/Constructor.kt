package token.invocation

import lexer.PositionalException
import properties.Type
import properties.primitive.PInt
import table.SymbolTable
import token.Identifier
import token.Token
import token.statement.Assignment
import utils.Utils.toProperty
import java.util.*

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
        resolveArguments(type, symbolTable)
        return if (resolving) type else resolveTree(type, symbolTable.changeVariable(type).changeScope())
    }

    private fun resolveArguments(type: Type, symbolTable: SymbolTable) {
        for (arg in children.subList(1, children.size)) {
            if (arg !is Assignment)
                throw PositionalException("Expected assignment", arg)
            if (arg.left !is Identifier)
                throw PositionalException("Expected property name", arg)
            type.setProperty(arg.left.value, arg.right.evaluate(symbolTable).toProperty(arg.left, type))
            type.removeAssignment(arg.left)
        }
    }

    private fun resolveTree(root: Type, symbolTable: SymbolTable): Type {
        root.setProperty("parent", PInt(0, root))
        resolving = true
        do {
            val (current, parent) = bfs(root) ?: break
            val stack = Stack<Assignment>()
            stack.add(current)
            processAssignment(parent, symbolTable.changeVariable(parent), stack)
        } while (true)
        resolving = false
        return root
    }

    private fun processAssignment(parent: Type, symbolTable: SymbolTable, stack: Stack<Assignment>) {
        while (stack.isNotEmpty()) {
            val unresolved = stack.pop()
            val top = unresolved.getFirstUnassigned(symbolTable, parent)
            if (top != null && top is Assignment)
                stack.add(top)
            else unresolved.assign(parent, symbolTable.changeVariable(parent))
        }
    }

    /**
     * Find unresolved assignments
     */
    private fun bfs(root: Type): Pair<Assignment, Type>? {
        val stack = Stack<Type>()
        val visited = Stack<Type>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val current = stack.pop()
            visited.add(current)
            if (current.assignments.isNotEmpty())
                return Pair(current.assignments.first(), current)
            val containers = current.getProperties().getPValue().values.filterIsInstance<Type>()
            stack.addAll(containers.filter { !visited.contains(it) })
        }
        return null
    }

    companion object {
        var resolving = false
    }
}
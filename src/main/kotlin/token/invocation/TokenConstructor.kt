package token.invocation

import SymbolTable
import SymbolTable.Type
import lexer.Parser
import token.Token
import token.TokenIdentifier
import token.statement.TokenAssignment
import java.util.*

class TokenConstructor(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?, children: List<Token>
) : TokenIdentifier(symbol, value, position, bindingPower, nud, led, std) {
    init {
        this.children.clear()
        this.children.addAll(children)
    }

    override fun evaluate(symbolTable: SymbolTable): Any {
        val type = symbolTable.getInvokable(left)
        return if (resolving) type as Type else resolveTree(type as Type)
    }

    fun resolveTree(root: Type): Type {
        resolving = true
        do {
            val current = bfs(root) ?: break
            processAssignment(current)
        } while (true)
        resolving = false
        return root
    }

    private fun processAssignment(start: TokenAssignment) {

    }

    /**
     * Find unresolved assignments
     */
    private fun bfs(root: Type): TokenAssignment? {
        val stack = Stack<Type>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val current = stack.pop()
            if (current.assignments.isNotEmpty())
                return current.assignments.first()
            val containers = current.symbolTable.getVariableValues().filterIsInstance<Type>()
            stack.addAll(containers)
        }
        return null
    }

    companion object {
        var resolving = false
    }
}
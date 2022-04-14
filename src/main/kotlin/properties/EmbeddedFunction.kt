package properties

import lexer.PositionalException
import token.Token
import SymbolTable

class EmbeddedFunction(
    name: String, args: List<String>,
    private val execute: (token: Token, arguments: SymbolTable) -> Any,
    private val argsRange: IntRange = 1..1,
) : Function(name, args, Token()) {

    private fun checkInRange(invocation: Token): Boolean = argsRange.contains(invocation.children.size - 1)

    fun executeFunction(token: Token, symbolTable: SymbolTable): Any {
        if (!checkInRange(token))
            throw PositionalException("expected $argsRange arguments, got ${token.children.size - 1}", token)
        return execute(token, symbolTable)
    }
}
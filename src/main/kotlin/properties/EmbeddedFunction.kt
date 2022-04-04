package properties

import lexer.PositionalException
import lexer.Token
import structure.SymbolTable

class EmbeddedFunction(
    name: String,
    args: List<String>,
    private val execute: (token: Token, arguments: SymbolTable) -> Any,
    private val argsRange: IntRange = 1..1,
) :
    Function(name, args, Token()) {

    private fun checkInRange(invocation: Token): Boolean = argsRange.contains(invocation.children.size - 1)
    private fun getArguments(token: Token, symbolTable: SymbolTable) =
        (token.children - token.children[0]).map { name ->
            symbolTable.variables[name.value] ?: throw PositionalException("${name.value} is not declared", name)
        }

    fun executeFunction(token: Token, symbolTable: SymbolTable): Any {
        if (!checkInRange(token))
            throw PositionalException("expected $argsRange arguments, got ${token.children.size - 1}", token)
        //val arguments = getArguments(token, symbolTable)
        val ex = execute(token, symbolTable)
        return ex
    }
}
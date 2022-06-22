package properties

import table.SymbolTable
import token.Token
import token.statement.Assignment

class EmbeddedFunction(
    name: String,
    args: List<Token>,
    namedArgs: List<Assignment> = listOf(),
    private val execute: (token: Token, arguments: SymbolTable) -> Any,
) : Function(name, args, namedArgs, Token()) {
    fun executeFunction(token: Token, symbolTable: SymbolTable): Any = execute(token, symbolTable)
}

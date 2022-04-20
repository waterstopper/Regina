package token.invocation

import evaluation.FunctionEvaluation.toVariable
import lexer.Parser
import properties.EmbeddedFunction
import table.SymbolTable
import token.Token
import token.TokenIdentifier

class TokenCall(
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

    private val arguments: List<Token>
        get() = children.subList(1, children.size)

    /**
     * For function evaluation, new scope table is needed, to which all arguments will be
     * passed, named like parameters
     */
    override fun evaluate(symbolTable: SymbolTable): Any {
        val function = symbolTable.getFunction(left)
        val newFileTable = symbolTable.getFileOfValue(left) { it.getFunctionOrNull(left.value) }

        val tableWithNewScope = SymbolTable(fileTable = newFileTable)
        argumentsToParameters(symbolTable, tableWithNewScope)

        if (function is EmbeddedFunction)
            return function.executeFunction(this, tableWithNewScope)
        return function.body.evaluate(tableWithNewScope)
    }

    /**
     * Write arguments to parameters
     */
    private fun argumentsToParameters(argTable: SymbolTable, paramTable: SymbolTable) {
        val function = paramTable.getFunction(left)
        for ((index, arg) in arguments.withIndex())
            paramTable.addVariable(function.params[index], arg.evaluate(argTable).toVariable(arg))
    }
}
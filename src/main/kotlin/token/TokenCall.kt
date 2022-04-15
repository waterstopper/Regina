package token

import evaluation.Evaluation
import evaluation.FunctionEvaluation
import evaluation.FunctionEvaluation.toVariable
import lexer.Parser
import properties.EmbeddedFunction
import SymbolTable

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

    val arguments: List<Token>
        get() = children.subList(1, children.size)

    override fun evaluate(symbolTable: SymbolTable): Any {
        val function = symbolTable.getFunction(left)

        val localTable = Evaluation.globalTable.copy()
        argumentsToParameters(symbolTable, localTable)
//        localTable.addVariables(children.subList(1, children.size).map {
//            it.evaluate(localTable).toVariable(it)
//        }, function.args)
        if (function is EmbeddedFunction)
            return function.executeFunction(left, localTable)
        return FunctionEvaluation.evaluateBlock(function.body, localTable)
    }

    /**
     * write arguments to parameters
     */
    fun argumentsToParameters(argTable: SymbolTable, paramTable: SymbolTable) {
        val function = paramTable.getFunction(left)
        for ((index, param) in function.args.withIndex())
            paramTable.addVariable(param, arguments[index].evaluate(argTable).toVariable(arguments[index]))
    }
}
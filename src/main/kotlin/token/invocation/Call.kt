package token.invocation

import lexer.Parser
import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import properties.Variable
import table.SymbolTable
import token.Identifier
import token.Token
import utils.Utils.toVariable

class Call(
    symbol: String, value: String, position: Pair<Int, Int>,
    bindingPower: Int, nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((token: Token, parser: Parser, token2: Token) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?, children: List<Token>
) : Identifier(symbol, value, position, bindingPower, nud, led, std) {

    init {
        this.children.clear()
        this.children.addAll(children)
    }

    val name: Token
        get() = left
    private val arguments: List<Token>
        get() = children.subList(1, children.size)
    var function: Function? = null

    /**
     * For function evaluation, new scope table is needed, to which all arguments will be
     * passed, named like parameters
     */
    override fun evaluate(symbolTable: SymbolTable): Any {
        val function = symbolTable.getFunction(left)
        val newTable =
            symbolTable.changeFile(symbolTable.getFileOfValue(left) { it.getFunctionOrNull(left.value) }.fileName)
        argumentsToParameters(this.function ?: function, symbolTable, newTable)
        return evaluateFunction(newTable, this.function ?: function)
    }

    /**
     * Write arguments to parameters
     */
    fun argumentsToParameters(function: Function, argTable: SymbolTable, paramTable: SymbolTable) {
        for ((index, arg) in arguments.withIndex())
            paramTable.addVariable(function.params[index], arg.evaluate(argTable).toVariable(arg))
    }

    fun evaluateFunction(symbolTable: SymbolTable, function: Function, variable: Variable? = null): Any {
        if (function.params.size < arguments.size)
            throw PositionalException("Expected less arguments", this)
        // wtf
        if (symbolTable.getVariableOrNull("(this)") != null)
            symbolTable.addVariable("(this)", symbolTable.getVariable("(this)"))
        if (variable != null)
            symbolTable.addVariable("(this)", variable)
        if (function is EmbeddedFunction)
            return function.executeFunction(this, symbolTable)
        return function.body.evaluate(symbolTable)
    }
}
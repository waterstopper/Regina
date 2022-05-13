package token.invocation

import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import table.SymbolTable
import token.Token
import utils.Utils.toVariable

class Call(
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

    fun evaluateFunction(symbolTable: SymbolTable, function: Function, argTable: SymbolTable? = null): Any {
        var argTable = argTable ?: symbolTable
        if (function.params.size < arguments.size)
            throw PositionalException("Expected less arguments", this)
        // wtf
        if (symbolTable.getVariableOrNull("(this)") != null)
            symbolTable.addVariable("(this)", symbolTable.getVariable("(this)"))
        if (symbolTable.getCurrentType() != null)
            symbolTable.addVariable("(this)", symbolTable.getCurrentType()!!)

        val res = if (function is EmbeddedFunction)
            function.executeFunction(this, symbolTable)
        else function.body.evaluate(symbolTable)
        return if (res is Unit) 0 else res
    }
}
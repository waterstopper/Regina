package token.invocation

import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import properties.Variable
import table.SymbolTable
import token.Token
import token.statement.Assignment
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

    val allArgs = children.subList(1, children.size)

    val unnamedArgs: List<Token>
        get() {
            val res = mutableListOf<Token>()
            var i = 1
            while (i < children.size && children[i] !is Assignment) {
                res.add(children[i])
                i++
            }
            return res
        } // children.subList(1, children.size)
    val namedArgs: List<Assignment>
        get() = children.subList(unnamedArgs.size + 1, children.size) as List<Assignment>

    /**
     * For function evaluation, new scope table is needed, to which all arguments will be
     * passed, named like parameters
     */
    override fun evaluate(symbolTable: SymbolTable): Any {
        val function = symbolTable.getFunction(this)
        val newTable =
            symbolTable.changeFile(symbolTable.getFileOfValue(left) { it.getFunctionOrNull(this) })
        argumentsToParameters(function, symbolTable, newTable)
        return evaluateFunction(newTable, function)
    }

    /**
     * Write arguments to parameters
     */
    fun argumentsToParameters(function: Function, argTable: SymbolTable, paramTable: SymbolTable) {
        val assigned = mutableMapOf<String, Variable>()
        for ((index, arg) in unnamedArgs.withIndex()) {
            val paramName = getParamName(index, function)
            if (assigned[paramName] != null)
                throw PositionalException("Argument already assigned", arg)
            assigned[paramName] = arg.evaluate(argTable).toVariable(arg)
        }
        for (nArg in namedArgs) {
            if (!function.hasParam(nArg.name))
                throw PositionalException("Parameter with name `${nArg.name}` absent", nArg)
            if (assigned[nArg.name] != null)
                throw PositionalException("Argument already assigned", nArg)
            assigned[nArg.name] = nArg.right.evaluate(argTable).toVariable(nArg)
        }
        for (defaultParam in function.defaultParams)
            if (assigned[defaultParam.name] == null)
                assigned[defaultParam.name] = defaultParam.right.evaluate(paramTable).toVariable(defaultParam)
        for (param in function.nonDefaultParams)
            if (assigned[param.value] == null)
                throw PositionalException("Parameter not assigned", param, file = paramTable.getFileTable().fileName)
        for (i in assigned)
            paramTable.addVariable(i.key, i.value)
        // TODO exceptions only to calls, not to params
//        for ((index, arg) in arguments.withIndex())
//            paramTable.addVariable(function.params[index].value, arg.evaluate(argTable).toVariable(arg))
    }

    private fun getParamName(index: Int, function: Function): String {
        return if (function.nonDefaultParams.lastIndex < index) {
            if (function.nonDefaultParams.size + function.defaultParams.lastIndex < index)
                throw PositionalException("More arguments than parameters", children[index])
            else function.defaultParams[index - function.nonDefaultParams.size].name
        } else function.nonDefaultParams[index].value
    }

    fun evaluateFunction(symbolTable: SymbolTable, function: Function, argTable: SymbolTable? = null): Any {
        var argTable = argTable ?: symbolTable
        //   if (function.params.size < arguments.size)
        //      throw PositionalException("Expected less arguments", this)
        // wtf
//        if (symbolTable.getVariableOrNull("this") != null)
//            symbolTable.addVariable("this", symbolTable.getVariable("this"))
//        if (symbolTable.getCurrentType() != null)
//            symbolTable.addVariable("this", symbolTable.getCurrentType()!!)

        val res = if (function is EmbeddedFunction)
            function.executeFunction(this, symbolTable)
        else function.body.evaluate(symbolTable)
        return if (res is Unit) 0 else res
    }
}

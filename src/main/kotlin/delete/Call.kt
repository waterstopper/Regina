package delete

import delete.invocation.Invocation
import properties.Function

class Call(
    name: String, val called: Function?, namedArgs: List<Assignment>,
    unnamedArgs: List<Identifier>,
    position: Pair<Int, Int>
) : Invocation(name, namedArgs, unnamedArgs, position) {
//    override fun evaluate(symbolTable: SymbolTable): Any {
//        val function = called ?: symbolTable.getFunction(this)
//
//        val newTable = symbolTable.changeFile(symbolTable.getFileOfValue(this)
//        { symbolTable.getFunctionOrNull(this) }).changeScope()
//        argumentsToParameters(function, symbolTable, newTable)
//        return evaluateFunction(newTable, function)
//    }
//
//    /**
//     * Write arguments to parameters
//     */
//    fun argumentsToParameters(function: Function, argTable: SymbolTable, paramTable: SymbolTable) {
//        val assigned = mutableMapOf<String, Variable>()
//        for ((index, arg) in unnamedArgs.withIndex()) {
//            val paramName = getParamName(index, function)
//            if (assigned[paramName] != null)
//                throw PositionalException("Argument already assigned", arg)
//            assigned[paramName] = arg.evaluate(argTable).toVariable(arg)
//        }
//        for (nArg in namedArgs) {
//            if (!function.hasParam(nArg.name))
//                throw PositionalException("Parameter with name `${nArg.name}` absent", nArg)
//            if (assigned[nArg.name] != null)
//                throw PositionalException("Argument already assigned", nArg)
//            assigned[nArg.name] = nArg.right.evaluate(argTable).toVariable(nArg)
//        }
//        for (defaultParam in function.defaultParams)
//            if (assigned[defaultParam.name] == null)
//                assigned[defaultParam.name] = defaultParam.right.evaluate(paramTable).toVariable(defaultParam)
//        for (param in function.nonDefaultParams)
//            if (assigned[param.value] == null)
//                throw PositionalException("Parameter not assigned", param, file = paramTable.getFileTable().fileName)
//        for (i in assigned)
//            paramTable.addVariable(i.key, i.value)
//        // TODO exceptions only to calls, not to params
////        for ((index, arg) in arguments.withIndex())
////            paramTable.addVariable(function.params[index].value, arg.evaluate(argTable).toVariable(arg))
//    }
//
//    private fun getParamName(index: Int, function: Function): String {
//        return if (function.nonDefaultParams.lastIndex < index) {
//            if (function.nonDefaultParams.size + function.defaultParams.lastIndex < index)
//                throw PositionalException("More arguments than parameters", children[index])
//            else function.defaultParams[index - function.nonDefaultParams.size].name
//        } else function.nonDefaultParams[index].value
//    }
//
//    fun evaluateFunction(symbolTable: SymbolTable, function: Function, argTable: SymbolTable? = null): Any {
//        var argTable = argTable ?: symbolTable
//        //   if (function.params.size < arguments.size)
//        //      throw PositionalException("Expected less arguments", this)
//        // wtf
////        if (symbolTable.getVariableOrNull("this") != null)
////            symbolTable.addVariable("this", symbolTable.getVariable("this"))
////        if (symbolTable.getCurrentType() != null)
////            symbolTable.addVariable("this", symbolTable.getCurrentType()!!)
//
//        val res = if (function is EmbeddedFunction)
//            function.executeFunction(this, symbolTable)
//        else function.body.evaluate(symbolTable)
//        // this is because Unit variables are not allowed and in Links variable is assigned
//        return if (res is Unit) 0 else res
//    }
}
package evaluation

import evaluation.Evaluation.rnd
import lexer.PositionalException
import token.Token
import properties.*
import properties.Function
import properties.primitive.*

object FunctionEvaluation {
    fun createFunction(token: Token, parent: Type? = null): Function {
        if (token.left.value != "(")
            throw PositionalException("expected parentheses after function name", token.left)
        val nameToken = token.left.left
        val body = token.children[1]
        val argsTokens = token.left.children - nameToken

        return Function(nameToken.value, argsTokens.map { it.value }, body, parent)
    }

//    fun evaluateFunction(token: Token, function: Function, args: List<Token>, symbolTable: SymbolTable): Any {
//        // this table is used for function execution. Hence, it should contain only function arguments
//        val localTable = globalTable.copy()
//        localTable.addVariables(args.map {
//            evaluateValue(
//                it,
//                symbolTable
//            ).toVariable(token)//(function.args[index])
//        }, function.params)
//        if (function is EmbeddedFunction)
//            return function.executeFunction(token, localTable)
//        return evaluateBlock(function.body, localTable)
//    }

//    fun evaluateBlock(token: Token, symbolTable: SymbolTable): Any {
//        for (stmt in token.children) {
//            when (stmt.value) {
//                //   "while" -> evaluateWhile(stmt, symbolTable)
//                //"if" -> evaluateIf(stmt, symbolTable)
//                // "=" -> stmt.evaluate(symbolTable)
//                // "(" -> stmt.evaluate(symbolTable)
////                "." -> {
////                    val func = symbolTable.getFunction(stmt)
////                    evaluateFunction(
////                        stmt.right, func, stmt.right.children.subList(1, stmt.right.children.size),
////                        SymbolTable(symbolTable.getVariables(), currentFile = stmt.left.value)
////                    )
////                    println(func)
////                }
//                // important to specifically evaluate it, because it will return different value
//                "return" -> {
//                    return if (stmt.children.size == 0)
//                        Unit
//                    else stmt.left.evaluate(symbolTable)
//                }
//                else -> stmt.evaluate(symbolTable)//throw PositionalException("expected assignment, invocation or block", stmt)
//            }
//        }
//        return Unit
//    }

//    private fun evaluateWhile(token: Token, symbolTable: SymbolTable) {
//        val condition = token.left
//        val block = token.right
//        while (evaluateValue(condition, symbolTable).toBoolean(condition)) {
//            evaluateBlock(block, symbolTable)
//        }
//    }

//    private fun evaluateIf(token: Token, symbolTable: SymbolTable) {
//        val condition = token.left
//        val trueBlock = token.right
//        if (evaluateValue(condition, symbolTable).toBoolean(condition))
//            evaluateBlock(trueBlock, symbolTable)
//        else if (token.children.size == 3)
//            evaluateBlock(token.children[2], symbolTable)
//    }

    fun Any.toVariable(token: Token, parent: Type? = null): Variable {
        if (this is Type)
            return this
        return Primitive.createPrimitive(this, parent, token)
    }

    fun initializeEmbedded(): MutableMap<String, Function> {
        val res = mutableMapOf<String, Function>()
        res["log"] = EmbeddedFunction("log", listOf("x"), { _, args ->
            println(args.getVariable("x"))
        })
        res["test"] = EmbeddedFunction("test", listOf("x"), { token, args ->
            if (args.getVariable("x") !is PInt || (args.getVariable("x") as PInt).value == 0)
                throw PositionalException("test failed", token)
        })
        res["rnd"] = EmbeddedFunction("rnd", listOf(), { _, _ ->
            rnd.nextDouble()
        }, 0..0)
        res["str"] = EmbeddedFunction("str", listOf("x"), { _, args ->
            args.getVariable("x").toString()
        })
        res["int"] = EmbeddedFunction("int", listOf("x"), { token, args ->
            when (val argument = args.getVariable("x")) {
                is PDouble -> (argument.value as Double).toInt()
                is PInt -> argument.value
                is PString -> (argument.value as String).toInt()
                else -> throw PositionalException("cannot cast type to integer", token)
            }
        })
        res["double"] = EmbeddedFunction("double", listOf("x"), { token, args ->
            when (val argument = args.getVariable("x")) {
                is PDouble -> (argument.value as Double)
                is PInt -> (argument.value as Int).toDouble()
                is PString -> (argument.value as String).toDouble()
                else -> throw PositionalException("cannot cast type to integer", token)
            }
        })

        return (res + PArray.initializeEmbeddedArrayFunctions()) as MutableMap<String, Function>
    }
}
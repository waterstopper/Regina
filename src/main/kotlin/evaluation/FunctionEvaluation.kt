package evaluation

import evaluation.Evaluation.evaluateInvocation
import evaluation.Evaluation.globalTable
import evaluation.Evaluation.rnd
import evaluation.TypeEvaluation.resolveTree
import lexer.PositionalException
import lexer.Token
import evaluation.ValueEvaluation.evaluateValue
import evaluation.ValueEvaluation.toBoolean
import properties.*
import properties.Assignment.Companion.evaluateAssignment
import properties.Function
import properties.primitive.*
import structure.*

object FunctionEvaluation {
    val functions = initializeEmbedded()

    fun createFunction(token: Token, parent: Type?): Function {
        if (token.children[0].value != "(")
            throw PositionalException("expected parentheses after function name", token.children[0])
        val nameToken = token.children[0].children[0]
        val body = token.children[1]
        val argsTokens = token.children[0].children - nameToken

        return Function(nameToken.value, argsTokens.map { it.value }, body, parent)
    }

    fun evaluateFunction(token: Token, function: Function, args: List<Token>, symbolTable: SymbolTable): Any {
        // this table is used for function execution. Hence, it should contain only function arguments
        val localTable = globalTable.copy()
        localTable.addVariables(args.map {
            evaluateValue(
                it,
                symbolTable
            ).toVariable(token)//(function.args[index])
        }, function.args)
        if (function is EmbeddedFunction)
            return function.executeFunction(token, localTable)
        return evaluateBlock(function.body, localTable)
    }

    fun evaluateBlock(token: Token, symbolTable: SymbolTable): Any {
        for (stmt in token.children) {
            when (stmt.value) {
                "while" -> evaluateWhile(stmt, symbolTable)
                "if" -> evaluateIf(stmt, symbolTable)
                "=" -> evaluateAssignment(stmt, symbolTable)
                "(" -> evaluateInvocation(stmt, symbolTable)
                "return" -> {
                    return if (stmt.children.size == 0)
                        Unit
                    else evaluateValue(stmt.children[0], symbolTable)
                }
                else -> throw PositionalException("expected assignment, invocation or block", stmt)
            }
        }
        return Unit
    }

    private fun evaluateWhile(token: Token, symbolTable: SymbolTable) {
        val condition = token.children[0]
        val block = token.children[1]
        while (evaluateValue(condition, symbolTable).toBoolean(condition)) {
            evaluateBlock(block, symbolTable)
        }
    }

    private fun evaluateIf(token: Token, symbolTable: SymbolTable) {
        val condition = token.children[0]
        val trueBlock = token.children[1]
        if (evaluateValue(condition, symbolTable).toBoolean(condition))
            evaluateBlock(trueBlock, symbolTable)
        else if (token.children.size == 3)
            evaluateBlock(token.children[2], symbolTable)
    }

    fun Any.toVariable(token: Token, parent: Type? = null): Variable {
        if (this is Type) {
            return resolveTree(this)
        }
        return Primitive.createPrimitive(this, parent, token)
    }

    private fun initializeEmbedded(): MutableMap<String, Function> {
        val res = mutableMapOf<String, Function>()
        res["log"] = EmbeddedFunction("log", listOf("x"), { _, args ->
            println(args.variables["x"])
        })
        res["rnd"] = EmbeddedFunction("rnd", listOf(), { _, _ ->
            rnd.nextDouble()
        }, 0..0)
        res["str"] = EmbeddedFunction("str", listOf("x"), { _, args ->
            args.variables["x"].toString()
        })
        res["int"] = EmbeddedFunction("int", listOf("x"), { token, args ->
            when (val argument = args.variables["x"]) {
                is PDouble -> (argument.value as Double).toInt()
                is PInt -> argument.value
                is PString -> (argument.value as String).toInt()
                else -> throw PositionalException("cannot cast type to integer", token)
            }
        })
        res["double"] = EmbeddedFunction("double", listOf("x"), { token, args ->
            when (val argument = args.variables["x"]) {
                is PDouble -> (argument.value as Double)
                is PInt -> (argument.value as Int).toDouble()
                is PString -> (argument.value as String).toDouble()
                else -> throw PositionalException("cannot cast type to integer", token)
            }
        })

        return (res + PArray.initializeEmbeddedArrayFunctions()) as MutableMap<String, Function>
    }

    fun addFunction(token: Token) {
        val func = Function(
            token.children[0].children[0].value,
            token.children[0].children.subList(1, token.children[0].children.size).map { it.value },
            token.children[1]
        )
        functions[token.children[0].children[0].value] = func
    }
}
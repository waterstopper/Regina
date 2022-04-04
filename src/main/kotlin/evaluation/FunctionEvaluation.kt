package evaluation

import evaluation.Evaluation.SEED
import evaluation.Evaluation.evaluateAssignment
import evaluation.Evaluation.evaluateInvocation
import evaluation.Evaluation.globalTable
import evaluation.Evaluation.rnd
import lexer.PositionalException
import lexer.Token
import evaluation.ValueEvaluation.evaluateIndex
import evaluation.ValueEvaluation.evaluateValue
import evaluation.ValueEvaluation.toInt
import properties.*
import properties.Function
import structure.*
import kotlin.random.Random.Default.nextDouble

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

    fun addFunction(token: Token) {
        val func = Function(
            token.children[0].children[0].value,
            token.children[0].children.subList(1, token.children[0].children.size).map { it.value },
            token.children[1]
        )
        functions[func.name] = func
    }

    fun evaluateFunction(token: Token, function: Function, args: List<Token>, symbolTable: SymbolTable): Any {
        // this table is used for function execution. Hence, it should contain only function arguments
        val localTable = globalTable.copy()
        localTable.addVariables(args.mapIndexed { index, it ->
            evaluateValue(
                it,
                symbolTable
            ).toVariable(function.args[index])
        }, function.args)
        if (function is EmbeddedFunction) {
            val refw = evaluateEmbeddedFunction(token, function, localTable)
            return refw
        }

        val res = evaluateBlock(function.body, localTable)
        println(res)
        return res
    }

    private fun evaluateEmbeddedFunction(token: Token, function: EmbeddedFunction, symbolTable: SymbolTable): Any {
        return function.executeFunction(token, symbolTable)
    }

    fun evaluateWhile(token: Token, symbolTable: SymbolTable) {}

    fun evaluateIf(token: Token, symbolTable: SymbolTable) {}

    fun evaluateBlock(token: Token, symbolTable: SymbolTable): Any {
        // val localSymbolTable = symbolTable.copy()
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

    private fun Any.toVariable(name: String, parent: Type? = null): Variable {
        if (this is Type) {
            return this
        }
        return Primitive(name, this, parent)
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
            val argument = args.variables["x"]
            if (argument is Primitive) {
                when (argument.value) {
                    is Double -> (argument.value as Double).toInt()
                    is Int -> argument.value
                    is String -> (argument.value as String).toInt()
                    else -> throw PositionalException("cannot cast array to integer", token)
                }
            } else throw PositionalException("cannot cast class to integer")
        })
        res["double"] = EmbeddedFunction("double", listOf("x"), { token, args ->
            val argument = args.variables["x"]
            if (argument is Primitive) {
                when (argument.value) {
                    is Double -> argument.value
                    is Int -> (argument.value as Int).toDouble()
                    is String -> (argument.value as String).toDouble()
                    else -> throw PositionalException("cannot cast array to double", token.children[1])
                }
            } else throw PositionalException("cannot cast class to double")
        })
        res["add"] = EmbeddedFunction("add", listOf("arr", "i", "x"), { token, args ->
            val list = args.variables["arr"]
            if (list is Primitive && list.value is MutableList<*>) {
                val argument = if (args.variables["x"] != null) args.variables["x"]!! else args.variables["i"]!!
                val indexVar: Any = args.variables["i"]!!
                var index = (list.value as MutableList<*>).size
                if (args.variables["x"] != null)
                    if (indexVar is Primitive && indexVar.value is Int) {
                        index = (indexVar.value as Int)
                    } else throw PositionalException("expected integer as index", token.children[2])
                (list.value as MutableList<Any>).add(index, argument)
            } else throw PositionalException("add is not applicable for this type", token.children[1])
        }, 2..3)
        res["remove"] = EmbeddedFunction("remove", listOf("arr", "x"), { token, args ->
            val list = args.variables["arr"]
            if (list is Primitive && list.value is MutableList<*>) {
                val argument = args.variables["x"]!!
                if (argument is Primitive) {
                    var removed = false
                    for (e in (list.value as MutableList<*>)) {
                        if (e is Primitive && e.value == argument.value) {
                            removed = true
                            (list.value as MutableList<*>).remove(e)
                            break
                        }
                    }
                    removed
                } else (list.value as MutableList<*>).remove(argument).toInt()
            } else throw PositionalException("remove is not applicable for this type", token.children[1])
        }, 2..2)
        res["removeAt"] = EmbeddedFunction("removeAt", listOf("arr", "i"), { token, args ->
            val list = args.variables["arr"]
            val index = args.variables["i"]!!
            if (list is Primitive && list.value is MutableList<*>) {
                if (index is Primitive && index.value is Int)
                    try {
                        (list.value as MutableList<*>).removeAt(index.value as Int)!!
                    } catch (e: IndexOutOfBoundsException) {
                        throw PositionalException("index ${index.value} out of bounds for length ${(list.value as MutableList<*>).size}")
                    }
                else throw PositionalException("expected integer as index", token.children[2])
            } else throw PositionalException("removeAt is not applicable for this type", token.children[1])
        }, 2..2)
        res["has"] = EmbeddedFunction("has", listOf("arr", "x"), { token, args ->
            val list = args.variables["arr"]
            val element = args.variables["x"]!!
            if (list is Primitive && list.value is MutableList<*>) {
                if (element is Primitive)
                    (list.value as MutableList<*>).any { (it is Primitive && it.value == element.value) }.toInt()
                else (list.value as MutableList<*>).any { it == element }.toInt()
            } else throw PositionalException("has is not applicable for this type", token.children[1])
        }, 2..2)
        return res
    }
}
package evaluation

import evaluation.Evaluation.rnd
import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import properties.primitive.PDictionary
import properties.primitive.PDouble
import properties.primitive.PInt
import properties.primitive.PString
import table.SymbolTable
import token.Token
import token.statement.Assignment
import utils.Utils.toVariable
import kotlin.math.cos
import kotlin.math.sin

object FunctionFactory {
    fun createFunction(token: Token): Function {
        if (token.left.value != "(")
            throw PositionalException("expected parentheses after function name", token.left)
        val withoutDefault = mutableListOf<Token>()
        val withDefault = mutableListOf<Assignment>()
        for (i in 1..token.left.children.lastIndex) {
            if (token.left.children[i] !is Assignment)
                withoutDefault.add(token.left.children[i])
            else withDefault.add(token.left.children[i] as Assignment)
        }
        return Function(
            name = token.left.left.value,
            params = withoutDefault,
            withDefaultParams = withDefault,
            body = token.children[1]
        )
    }

    fun createIdent(token: Token, name: String) = Token(value = name, symbol = name, position = token.position)

    fun getIdent(token: Token, name: String, args: SymbolTable) = args.getIdentifier(createIdent(token, name))


    fun initializeEmbedded(): MutableMap<String, Function> {
        val res = mutableMapOf<String, Function>()
        res["log"] = EmbeddedFunction("log", listOf(Token(value = "x")))
        { token, args -> println(getIdent(token, "x", args)) }
        res["input"] = EmbeddedFunction("input", listOf()) { _, _ -> readLine() ?: "" }
        res["test"] = EmbeddedFunction("test", listOf(Token(value = "x"))) { token, args ->
            val ident = getIdent(token, "x", args)
            if (ident !is PInt || ident.getPValue() == 0)
                throw PositionalException("test failed", token)
        }
        res["rnd"] = EmbeddedFunction("rnd", listOf()) { _, _ -> rnd.nextDouble() }
        res["str"] = EmbeddedFunction("str", listOf(Token(value = "x")))
        { token, args -> getIdent(token, "x", args).toString() }
        res["int"] = EmbeddedFunction("int", listOf(Token(value = "x"))) { token, args ->
            when (val argument = getIdent(token, "x", args)) {
                is PDouble -> argument.getPValue().toInt()
                is PInt -> argument.getPValue()
                is PString -> argument.getPValue().toInt()
                else -> throw PositionalException("cannot cast type to integer", token)
            }
        }
        res["double"] = EmbeddedFunction("double", listOf(Token(value = "x"))) { token, args ->
            when (val argument = getIdent(token, "x", args)) {
                is PDouble -> argument.getPValue()
                is PInt -> argument.getPValue().toDouble()
                is PString -> argument.getPValue().toDouble()
                else -> throw PositionalException("cannot cast type to double", token)
            }
        }
        res["array"] = EmbeddedFunction("array", listOf(Token(value = "x"))) { token, args ->
            when (val argument = getIdent(token, "x", args)) {
                is PDictionary -> argument.getPValue()
                    .map {
                        PDictionary(
                            mutableMapOf(
                                "key" to it.key.toVariable(token),
                                "value" to it.value.toVariable(token)
                            ), null
                        )
                    }
                is PString -> argument.getPValue().map { it.toString() }
                else -> throw PositionalException("cannot cast type to array", token)
            }
        }
        res["sin"] = EmbeddedFunction("sin", listOf(Token("angle"))) { token, args ->
            when (val argument = getIdent(token, "angle", args)) {
                is PInt -> sin(argument.getPValue().toDouble())
                is PDouble -> sin(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        res["cos"] = EmbeddedFunction("cos", listOf(Token("angle"))) { token, args ->
            when (val argument = getIdent(token, "angle", args)) {
                is PInt -> cos(argument.getPValue().toDouble())
                is PDouble -> cos(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        return res
    }
}
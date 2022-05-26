package evaluation

import evaluation.Evaluation.rnd
import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import properties.primitive.PDictionary
import properties.primitive.PDouble
import properties.primitive.PInt
import properties.primitive.PString
import token.Token
import token.statement.Assignment
import utils.Utils.toVariable

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

    fun initializeEmbedded(): MutableMap<String, Function> {
        val res = mutableMapOf<String, Function>()
        res["log"] = EmbeddedFunction("log", listOf(Token(value = "x")))
        { _, args -> println(args.getVariable("x")) }
        res["test"] = EmbeddedFunction("test", listOf(Token(value = "x"))) { token, args ->
            if (args.getVariable("x") !is PInt || (args.getVariable("x") as PInt).getPValue() == 0)
                throw PositionalException("test failed", token)
        }
        res["rnd"] = EmbeddedFunction("rnd", listOf()) { _, _ -> rnd.nextDouble() }
        res["str"] = EmbeddedFunction("str", listOf(Token(value = "x")))
        { _, args -> args.getVariable("x").toString() }
        res["int"] = EmbeddedFunction("int", listOf(Token(value = "x"))) { token, args ->
            when (val argument = args.getVariable("x")) {
                is PDouble -> argument.getPValue().toInt()
                is PInt -> argument.getPValue()
                is PString -> argument.getPValue().toInt()
                else -> throw PositionalException("cannot cast type to integer", token)
            }
        }
        res["double"] = EmbeddedFunction("double", listOf(Token(value = "x"))) { token, args ->
            when (val argument = args.getVariable("x")) {
                is PDouble -> argument.getPValue()
                is PInt -> argument.getPValue().toDouble()
                is PString -> argument.getPValue().toDouble()
                else -> throw PositionalException("cannot cast type to double", token)
            }
        }
        res["array"] = EmbeddedFunction("array", listOf(Token(value = "x"))) { token, args ->
            when (val argument = args.getVariable("x")) {
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
        return res
    }
}
package evaluation

import evaluation.Evaluation.rnd
import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import properties.Type
import properties.Variable
import properties.primitive.PDouble
import properties.primitive.PInt
import properties.primitive.PString
import properties.primitive.Primitive
import token.Token

object FunctionEvaluation {
    fun createFunction(token: Token): Function {
        if (token.left.value != "(")
            throw PositionalException("expected parentheses after function name", token.left)
        val nameToken = token.left.left
        val body = token.children[1]
        val argsTokens = token.left.children - nameToken
        return Function(nameToken.value, argsTokens.map { it.value }, body)
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
        return res
    }
}
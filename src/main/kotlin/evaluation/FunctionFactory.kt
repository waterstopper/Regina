package evaluation

import lexer.ExpectedTypeException
import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import properties.primitive.*
import table.SymbolTable
import token.Token
import token.statement.Assignment
import utils.Utils
import utils.Utils.toInt
import utils.Utils.toVariable
import java.io.File
import kotlin.math.*
import kotlin.random.Random

object FunctionFactory {
    private var randomSeed = 42
    private var rnd = Random(randomSeed)

    fun createFunction(token: Token): Function {
        if (token.left.value != "(")
            throw PositionalException("Expected parentheses after function name", token.left)
        val withoutDefault = mutableListOf<Token>()
        val withDefault = mutableListOf<Assignment>()
        for (i in 1..token.left.children.lastIndex) {
            if (token.left.children[i] !is Assignment)
                withoutDefault.add(token.left.children[i])
            else withDefault.add(token.left.children[i] as Assignment)
        }
        return Function(
            name = token.left.left.value,
            nonDefaultParams = withoutDefault,
            defaultParams = withDefault,
            body = token.children[1]
        )
    }

    private fun createIdent(token: Token, name: String) = Token(value = name, symbol = name, position = token.position)

    fun getIdent(token: Token, name: String, args: SymbolTable) = args.getIdentifier(createIdent(token, name))
    fun getDictionary(token: Token, name: String, args: SymbolTable): PDictionary {
        val dictionary = getIdent(token, name, args)
        if (dictionary !is PDictionary)
            throw PositionalException("Expected array as $name", token)
        return dictionary
    }

    fun getArray(token: Token, name: String, args: SymbolTable): PArray {
        val array = getIdent(token, name, args)
        if (array !is PArray)
            throw PositionalException("Expected array as $name", token)
        return array
    }

    fun getString(token: Token, name: String, args: SymbolTable): PString {
        val str = getIdent(token, name, args)
        if (str !is PString)
            throw PositionalException("Expected string as $name", token)
        return str
    }

    fun getNumber(token: Token, name: String, args: SymbolTable): PNumber {
        val num = getIdent(token, name, args)
        if (num !is PNumber)
            throw PositionalException("Expected integer as $name", token)
        return num
    }

    fun getInt(token: Token, name: String, args: SymbolTable): PInt {
        val int = getIdent(token, name, args)
        if (int !is PInt)
            throw PositionalException("Expected integer as $name", token)
        return int
    }

    fun getDouble(token: Token, name: String, args: SymbolTable): PDouble {
        val double = getIdent(token, name, args)
        if (double !is PDouble)
            throw PositionalException("Expected integer as $name", token)
        return double
    }

    fun initializeEmbedded(): MutableMap<String, Function> {
        val res = mutableMapOf<String, Function>()
        res["log"] = EmbeddedFunction("log", listOf(Token(value = "x")))
        { token, args -> println(getIdent(token, "x", args)) }
        res["except"] = EmbeddedFunction("except", listOf(Token(value = "x")))
        { token, args ->
            // TODO check that instances work properly. e.g. except(a())
            throw PositionalException(getIdent(token, "x", args).toString(), token)
        }
        res["input"] = EmbeddedFunction("input", listOf()) { _, _ -> readLine() ?: "" }
        res["write"] = EmbeddedFunction(
            "write",
            listOf(
                Token(value = "content"),
                Token(value = "path")
            )
        ) { token, args ->
            val fileName = getIdent(token, "path", args)
            val content = getIdent(token, "content", args)
            if (fileName !is PString || content !is PString)
                throw ExpectedTypeException(listOf(PString::class), token)
            val file = File(fileName.getPValue())
            file.writeText(content.getPValue())
        }
        res["read"] = EmbeddedFunction(
            "read",
            listOf(Token(value = "path"))
        ) { token, args ->
            val fileName = getIdent(token, "path", args)
            if (fileName !is PString)
                throw ExpectedTypeException(listOf(PString::class), token)
            val file = File(fileName.getPValue())
            file.readText()
        }
        res["exists"] = EmbeddedFunction(
            "exists",
            listOf(Token(value = "path"))
        ) { token, args ->
            val fileName = getIdent(token, "path", args)
            if (fileName !is PString)
                throw ExpectedTypeException(listOf(PString::class), token)
            val file = File(fileName.getPValue())
            file.exists().toInt()
        }
        res["delete"] = EmbeddedFunction(
            "delete",
            listOf(Token(value = "path"))
        ) { token, args ->
            val fileName = getIdent(token, "path", args)
            if (fileName !is PString)
                throw ExpectedTypeException(listOf(PString::class), token)
            val file = File(fileName.getPValue())
            file.delete().toInt()
        }
        res["test"] = EmbeddedFunction("test", listOf(Token(value = "x"))) { token, args ->
            val ident = getIdent(token, "x", args)
            if (ident !is PInt || ident.getPValue() == 0)
                throw PositionalException("test failed", token)
        }
        res["rnd"] = EmbeddedFunction("rnd", listOf()) { _, _ -> rnd.nextDouble() }
        res["seed"] = EmbeddedFunction("seed", listOf(Token(value = "x"))) { token, args ->
            val seed = getIdent(token, "x", args)
            if (seed !is PInt)
                throw ExpectedTypeException(listOf(PInt::class), token)
            randomSeed = seed.getPValue()
            rnd = Random(randomSeed)
            Unit
        }
        res["str"] =
            EmbeddedFunction("str", listOf(Token(value = "x"))) { token, args -> getIdent(token, "x", args).toString() }
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
                                PString("key", null) to it.key.toVariable(token),
                                PString("value", null) to it.value.toVariable(token)
                            ),
                            null
                        )
                    }
                is PString -> argument.getPValue().map { it.toString().toVariable() }
                else -> throw PositionalException("cannot cast type to array", token)
            }
        }
        res["sin"] = EmbeddedFunction("sin", listOf(Token(value = "angle"))) { token, args ->
            when (val argument = getIdent(token, "angle", args)) {
                is PInt -> sin(argument.getPValue().toDouble())
                is PDouble -> sin(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        res["cos"] = EmbeddedFunction("cos", listOf(Token(value = "angle"))) { token, args ->
            when (val argument = getIdent(token, "angle", args)) {
                is PInt -> cos(argument.getPValue().toDouble())
                is PDouble -> cos(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        res["sqrt"] = EmbeddedFunction("sqrt", listOf(Token(value = "number"))) { token, args ->
            when (val argument = getIdent(token, "number", args)) {
                is PInt -> sqrt(argument.getPValue().toDouble())
                is PDouble -> sqrt(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        res["asin"] = EmbeddedFunction("asin", listOf(Token(value = "sin"))) { token, args ->
            when (val argument = getIdent(token, "sin", args)) {
                is PInt -> asin(argument.getPValue().toDouble())
                is PDouble -> asin(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        res["acos"] = EmbeddedFunction("acos", listOf(Token(value = "cos"))) { token, args ->
            when (val argument = getIdent(token, "cos", args)) {
                is PInt -> acos(argument.getPValue().toDouble())
                is PDouble -> acos(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        res["floatEquals"] =
            EmbeddedFunction(
                "floatEquals", listOf(Token(value = "first"), Token(value = "second")),
                listOf(
                    Utils.parseAssignment("epsilon = 0.0000000000000000000000000001"),
                    Utils.parseAssignment("absTh = 0.0000001")
                )
            ) { token, args ->
                // https://stackoverflow.com/a/32334103
                val first = getNumber(token, "first", args).getPValue().toDouble()
                val second = getNumber(token, "second", args).getPValue().toDouble()
                val epsilon = getNumber(token, "epsilon", args).getPValue().toDouble()
                val absTh = getNumber(token, "absTh", args).getPValue().toDouble()
                if (first == second)
                    PInt(1, null)
                else {
                    val diff = abs(first - second)
                    val norm = min(abs(first) + abs(second), Float.MAX_VALUE.toDouble())
                    (diff < max(absTh, epsilon * norm)).toInt()
                }
            }
        return res
    }
}

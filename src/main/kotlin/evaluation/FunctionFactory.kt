package evaluation

import lexer.ExpectedTypeException
import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import properties.primitive.*
import table.SymbolTable
import token.Token
import token.statement.Assignment
import utils.Utils.toVariable
import java.io.File
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
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
            {// TODO check that instances work properly
                throw PositionalException(getIdent(token, "x", args).toString(), token)
            }
        }
        res["input"] = EmbeddedFunction("input", listOf()) { _, _ -> readLine() ?: "" }
        res["write"] = EmbeddedFunction(
            "write",
            listOf(
                Token(value = "content"),
                Token(value = "fileName")
            )
        ) { token, args ->
            val fileName = getIdent(token, "fileName", args)
            val content = getIdent(token, "content", args)
            if (fileName !is PString || content !is PString)
                throw ExpectedTypeException(listOf(PString::class), token)
            val file = File(fileName.getPValue())
            file.writeText(content.getPValue())
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
                                "key" to it.key.toVariable(token),
                                "value" to it.value.toVariable(token)
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
        return res
    }
}

package evaluation

import lexer.ExpectedTypeException
import lexer.PositionalException
import node.Identifier
import node.Node
import node.statement.Assignment
import properties.EmbeddedFunction
import properties.Function
import properties.primitive.*
import table.SymbolTable
import utils.Utils.toInt
import utils.Utils.toVariable
import java.io.File
import kotlin.math.*
import kotlin.random.Random

object FunctionFactory {
    private var randomSeed = 42
    private var rnd = Random(randomSeed)

    fun createFunction(node: Node): Function {
        if (node.left.value != "(")
            throw PositionalException("Expected parentheses after function name", node.left)
        val withoutDefault = mutableListOf<Identifier>()
        val withDefault = mutableListOf<Assignment>()
        for (i in 1..node.left.children.lastIndex) {
            if (node.left.children[i] !is Assignment)
                withoutDefault.add(node.left.children[i] as Identifier)
            else withDefault.add(node.left.children[i] as Assignment)
        }
        return Function(
            name = node.left.left.value,
            nonDefaultParams = withoutDefault,
            defaultParams = withDefault,
            body = node.children[1]
        )
    }

    private fun createIdent(node: Node, name: String) = Node(symbol = name, value = name, position = node.position)

    fun getIdent(node: Node, name: String, args: SymbolTable) = args.getIdentifier(createIdent(node, name))
    fun getDictionary(node: Node, name: String, args: SymbolTable): PDictionary {
        val dictionary = getIdent(node, name, args)
        if (dictionary !is PDictionary)
            throw PositionalException("Expected array as $name", node)
        return dictionary
    }

    fun getArray(node: Node, name: String, args: SymbolTable): PArray {
        val array = getIdent(node, name, args)
        if (array !is PArray)
            throw PositionalException("Expected array as $name", node)
        return array
    }

    fun getString(node: Node, name: String, args: SymbolTable): PString {
        val str = getIdent(node, name, args)
        if (str !is PString)
            throw PositionalException("Expected string as $name", node)
        return str
    }

    fun getNumber(node: Node, name: String, args: SymbolTable): PNumber {
        val num = getIdent(node, name, args)
        if (num !is PNumber)
            throw PositionalException("Expected integer as $name", node)
        return num
    }

    fun getInt(node: Node, name: String, args: SymbolTable): PInt {
        val int = getIdent(node, name, args)
        if (int !is PInt)
            throw PositionalException("Expected integer as $name", node)
        return int
    }

    fun getDouble(node: Node, name: String, args: SymbolTable): PDouble {
        val double = getIdent(node, name, args)
        if (double !is PDouble)
            throw PositionalException("Expected integer as $name", node)
        return double
    }

    fun initializeEmbedded(): MutableMap<String, Function> {
        val res = mutableMapOf<String, Function>()
        res["log"] = EmbeddedFunction("log", listOf("x"))
        { token, args -> println(getIdent(token, "x", args)) }
        res["except"] = EmbeddedFunction("except", listOf("x"))
        { token, args ->
            // TODO check that instances work properly. e.g. except(a())
            throw PositionalException(getIdent(token, "x", args).toString(), token)
        }
        res["input"] = EmbeddedFunction("input", listOf()) { _, _ -> readLine() ?: "" }
        res["write"] = EmbeddedFunction("write", listOf("content", "path")) { token, args ->
            val fileName = getIdent(token, "path", args)
            val content = getIdent(token, "content", args)
            if (fileName !is PString || content !is PString)
                throw ExpectedTypeException(listOf(PString::class), token)
            val file = File(fileName.getPValue())
            file.writeText(content.getPValue())
        }
        res["read"] = EmbeddedFunction("read", listOf("path")) { token, args ->
            val fileName = getIdent(token, "path", args)
            if (fileName !is PString)
                throw ExpectedTypeException(listOf(PString::class), token)
            val file = File(fileName.getPValue())
            file.readText()
        }
        res["exists"] = EmbeddedFunction("exists", listOf("path")) { token, args ->
            val fileName = getIdent(token, "path", args)
            if (fileName !is PString)
                throw ExpectedTypeException(listOf(PString::class), token)
            val file = File(fileName.getPValue())
            file.exists().toInt()
        }
        res["delete"] = EmbeddedFunction("delete", listOf("path")) { token, args ->
            val fileName = getIdent(token, "path", args)
            if (fileName !is PString)
                throw ExpectedTypeException(listOf(PString::class), token)
            val file = File(fileName.getPValue())
            file.delete().toInt()
        }
        res["test"] = EmbeddedFunction("test", listOf("x")) { token, args ->
            val ident = getIdent(token, "x", args)
            if (ident !is PInt || ident.getPValue() == 0)
                throw PositionalException("test failed", token)
        }
        res["rnd"] = EmbeddedFunction("rnd", listOf()) { _, _ -> rnd.nextDouble() }
        res["seed"] = EmbeddedFunction("seed", listOf("x")) { token, args ->
            val seed = getIdent(token, "x", args)
            if (seed !is PInt)
                throw ExpectedTypeException(listOf(PInt::class), token)
            randomSeed = seed.getPValue()
            rnd = Random(randomSeed)
            Unit
        }
        res["str"] =
            EmbeddedFunction("str", listOf("x")) { token, args -> getIdent(token, "x", args).toString() }
        res["int"] = EmbeddedFunction("int", listOf("x")) { token, args ->
            when (val argument = getIdent(token, "x", args)) {
                is PDouble -> argument.getPValue().toInt()
                is PInt -> argument.getPValue()
                is PString -> argument.getPValue().toInt()
                else -> throw PositionalException("cannot cast type to integer", token)
            }
        }
        res["double"] = EmbeddedFunction("double", listOf("x")) { token, args ->
            when (val argument = getIdent(token, "x", args)) {
                is PDouble -> argument.getPValue()
                is PInt -> argument.getPValue().toDouble()
                is PString -> argument.getPValue().toDouble()
                else -> throw PositionalException("cannot cast type to double", token)
            }
        }
        res["array"] = EmbeddedFunction("array", listOf("x")) { token, args ->
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
        res["sin"] = EmbeddedFunction("sin", listOf("angle")) { token, args ->
            when (val argument = getIdent(token, "angle", args)) {
                is PInt -> sin(argument.getPValue().toDouble())
                is PDouble -> sin(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        res["cos"] = EmbeddedFunction("cos", listOf("angle")) { token, args ->
            when (val argument = getIdent(token, "angle", args)) {
                is PInt -> cos(argument.getPValue().toDouble())
                is PDouble -> cos(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        res["sqrt"] = EmbeddedFunction("sqrt", listOf("number")) { token, args ->
            when (val argument = getIdent(token, "number", args)) {
                is PInt -> sqrt(argument.getPValue().toDouble())
                is PDouble -> sqrt(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        res["asin"] = EmbeddedFunction("asin", listOf("sin")) { token, args ->
            when (val argument = getIdent(token, "sin", args)) {
                is PInt -> asin(argument.getPValue().toDouble())
                is PDouble -> asin(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        res["acos"] = EmbeddedFunction("acos", listOf("cos")) { token, args ->
            when (val argument = getIdent(token, "cos", args)) {
                is PInt -> acos(argument.getPValue().toDouble())
                is PDouble -> acos(argument.getPValue())
                else -> throw PositionalException("Expected number", token)
            }
        }
        res["floatEquals"] =
            EmbeddedFunction(
                "floatEquals", listOf("first", "second"),
                listOf("epsilon = 0.0000000000000000000000000001", "absTh = 0.0000001")
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

package evaluation

import FileSystem
import Message
import lexer.ExpectedTypeException
import lexer.PositionalException
import node.Identifier
import node.Node
import node.statement.Assignment
import properties.EmbeddedFunction
import properties.RFunction
import properties.Type
import properties.primitive.*
import readLine
import sendMessage
import table.FileTable
import utils.Utils.NULL
import utils.Utils.TRUE
import utils.Utils.getIdent
import utils.Utils.getPInt
import utils.Utils.getPNumber
import utils.Utils.toPInt
import utils.Utils.toVariable
import kotlin.math.*
import kotlin.random.Random

object FunctionFactory {
    private var randomSeed = 42
    private var rnd = Random(randomSeed)

    fun createFunction(node: Node, fileTable: FileTable): RFunction {
        if (node.left.value != "(")
            throw PositionalException("Expected parentheses after function name", fileTable.filePath, node.left)
        val withoutDefault = mutableListOf<Identifier>()
        val withDefault = mutableListOf<Assignment>()
        for (i in 1..node.left.children.lastIndex) {
            if (node.left.children[i] !is Assignment)
                withoutDefault.add(node.left.children[i] as Identifier)
            else withDefault.add(node.left.children[i] as Assignment)
        }
        return RFunction(
            name = node.left.left.value,
            nonDefaultParams = withoutDefault,
            defaultParams = withDefault,
            body = node.children[1]
        )
    }

    fun initializeEmbedded(): MutableMap<String, RFunction> {
        val res = mutableMapOf<String, RFunction>()
        res["log"] = EmbeddedFunction("log", listOf("x"))
        { token, args ->
            sendMessage(Message("log", getIdent(token, "x", args).toString()))
            NULL
        }
        res["except"] = EmbeddedFunction("except", listOf("x"))
        { token, args ->
            // TODO check that instances work properly. e.g. except(a())
            throw PositionalException(getIdent(token, "x", args).toString(), args.getFileTable().filePath, token)
        }
        res["input"] = EmbeddedFunction("input", listOf()) { _, _ -> readLine() }
        res["write"] = EmbeddedFunction("write", listOf("content", "path")) { token, args ->
            val fileName = getIdent(token, "path", args)
            val content = getIdent(token, "content", args)
            if (fileName !is PString || content !is PString)
                throw ExpectedTypeException(listOf(PString::class), args.getFileTable().filePath, token)
            FileSystem.write(fileName.getPValue(), content.getPValue())
            NULL
        }
        res["read"] = EmbeddedFunction("read", listOf("path")) { token, args ->
            val fileName = getIdent(token, "path", args)
            if (fileName !is PString)
                throw ExpectedTypeException(listOf(PString::class), args.getFileTable().filePath, token)
            FileSystem.read(fileName.getPValue())
        }
        res["exists"] = EmbeddedFunction("exists", listOf("path")) { token, args ->
            val fileName = getIdent(token, "path", args)
            if (fileName !is PString)
                throw ExpectedTypeException(listOf(PString::class), args.getFileTable().filePath, token)
            FileSystem.exists(fileName.getPValue()).toPInt()
        }
        res["delete"] = EmbeddedFunction("delete", listOf("path")) { token, args ->
            val fileName = getIdent(token, "path", args)
            if (fileName !is PString)
                throw ExpectedTypeException(listOf(PString::class), args.getFileTable().filePath, token)
            FileSystem.delete(fileName.getPValue()).toPInt()
        }
        res["test"] = EmbeddedFunction("test", listOf("x")) { token, args ->
            val ident = getIdent(token, "x", args)
            if (ident !is PNumber || ident.getPValue() == 0)
                throw PositionalException("test failed", args.getFileTable().filePath, token)
            NULL
        }
        res["rnd"] = EmbeddedFunction("rnd", namedArgs = listOf("isInt = false")) { token, args ->
            if (getPInt(args, token, "isInt").getPValue() == 0)
                 PDouble(rnd.nextDouble()) else PInt(rnd.nextInt())
        }
        res["seed"] = EmbeddedFunction("seed", listOf("x")) { token, args ->
            val seed = getIdent(token, "x", args)
            if (seed !is PInt)
                throw ExpectedTypeException(listOf(PInt::class), args.getFileTable().filePath, token)
            randomSeed = seed.getPValue()
            rnd = Random(randomSeed)
            NULL
        }
        res["str"] =
            EmbeddedFunction("str", listOf("x")) { token, args -> getIdent(token, "x", args).toString() }
        res["int"] = EmbeddedFunction("int", listOf("x")) { token, args ->
            when (val argument = getIdent(token, "x", args)) {
                is PNumber -> PInt(argument.getPValue().toInt())
                is PString -> try {
                    PInt(argument.getPValue().toInt())
                } catch (e: NumberFormatException) {
                    throw PositionalException("String is not castable to int", args.getFileTable().filePath, token)
                }
                else -> throw PositionalException("Cannot cast type to double", args.getFileTable().filePath, token)
            }
        }
        res["double"] = EmbeddedFunction("double", listOf("x")) { token, args ->
            when (val argument = getIdent(token, "x", args)) {
                is PNumber -> PDouble(argument.getPValue().toDouble())
                is PString -> try {
                    PDouble(argument.getPValue().toDouble())
                } catch (e: NumberFormatException) {
                    throw PositionalException("String is not castable to Double", args.getFileTable().filePath, token)
                }
                else -> throw PositionalException("Cannot cast type to Double", args.getFileTable().filePath, token)
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
                            null,
                            Primitive.dictionaryId++
                        )
                    }
                is PString -> argument.getPValue().map { it.toString().toVariable() }
                else -> throw PositionalException("cannot cast type to array", args.getFileTable().filePath, token)
            }
        }
        res["floatEquals"] =
            EmbeddedFunction(
                "floatEquals", listOf("first", "second"),
                listOf("epsilon = 0.0000000000000000000000000001", "absTh = 0.0000001")
            ) { token, args ->
                // https://stackoverflow.com/a/32334103
                val first = getPNumber(args, token, "first").getPValue().toDouble()
                val second = getPNumber(args, token, "second").getPValue().toDouble()
                val epsilon = getPNumber(args, token, "epsilon").getPValue().toDouble()
                val absTh = getPNumber(args, token, "absTh").getPValue().toDouble()
                if (first == second)
                    TRUE
                else {
                    val diff = abs(first - second)
                    val norm = min(abs(first) + abs(second), Float.MAX_VALUE.toDouble())
                    (diff < max(absTh, epsilon * norm)).toPInt()
                }
            }
        res["type"] =
            EmbeddedFunction(
                "type", listOf("instance"),
            ) { token, args ->
                when (val instance = getIdent(token, "instance", args)) {
                    is PInt -> "Int"
                    is PDouble -> "Double"
                    is PString -> "String"
                    is PArray -> "Array"
                    is PDictionary -> "Dictionary"
                    is Type -> instance.fileTable.getUncopiedType(Node(value = instance.name))
                        ?: throw PositionalException("Class not found", args.getFileTable().filePath, token)
                    else -> throw PositionalException("Unsupported type", args.getFileTable().filePath, token)
                }
            }
        res["range"] = EmbeddedFunction("range", listOf("start", "end"), listOf("step = 1")) { token, args ->
            val start = getPInt(args, token, "start")
            val end = getPInt(args, token, "end")
            val step = getPInt(args, token, "step")
            if (step.getPValue() < 1)
                throw PositionalException("Step must be positive", args.getFileTable().filePath, token)
            mutableListOf(start, end, step).toVariable()
        }
        return res
    }
}

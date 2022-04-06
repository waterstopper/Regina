package properties.primitive

import evaluation.ValueEvaluation.toInt
import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Function
import properties.Type

class PArray(value: MutableList<Any>, parent: Type?) : Primitive("", value, parent) {
    companion object {
        fun initializeEmbeddedArrayFunctions(): MutableMap<String, Function> {
            val res = mutableMapOf<String, Function>()
            res["add"] = EmbeddedFunction("add", listOf("arr", "i", "x"), { token, args ->
                val list = args.variables["arr"]
                if (list is PArray) {
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
                if (list is PArray) {
                    val argument = args.variables["x"]!!
                    if (argument is Primitive) {
                        var removed = false
                        for (e in (list.value as MutableList<*>)) {
                            if (e is Primitive && e == argument) {
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
                if (list is PArray) {
                    if (index is PInt)
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
                if (list is PArray) {
                    if (element is Primitive)
                        (list.value as MutableList<*>).any { (it is Primitive && it == element) }.toInt()
                    else (list.value as MutableList<*>).any { it == element }.toInt()
                } else throw PositionalException("has is not applicable for this type", token.children[1])
            }, 2..2)

            return res
        }
    }
}


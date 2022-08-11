package properties.primitive

import evaluation.FunctionFactory.getArray
import evaluation.FunctionFactory.getIdent
import evaluation.FunctionFactory.getInt
import evaluation.FunctionFactory.getString
import lexer.PositionalException
import node.Node
import properties.EmbeddedFunction
import properties.Object
import properties.Type
import properties.Variable
import utils.Utils.castToArray
import utils.Utils.toInt
import utils.Utils.toProperty
import utils.Utils.toVariable

class PArray(value: MutableList<Variable>, parent: Type?) : Primitive(value, parent), Indexable {
    override fun getIndex() = 5
    override fun getPValue() = value as MutableList<Variable>
    override fun get(index: Any, node: Node): Any {
        if (index !is Int)
            throw PositionalException("Expected integer as index", node)
        if (index < 0 || index >= getPValue().size)
            throw PositionalException("Index out of bounds", node)
        return getPValue()[index]
    }

    override fun set(index: Any, value: Any, nodeIndex: Node, nodeValue: Node) {
        getPValue()[(index as PInt).getPValue()] = value.toVariable(nodeIndex)
    }

    override fun toString(): String {
        val res = StringBuilder("[")
        for (e in getPValue())
            res.append("${if (e == this) "this" else e.toString()}, ")
        if (res.toString() == "[")
            return "[]"
        return res.removeRange(res.lastIndex - 1..res.lastIndex).toString() + ']'
    }

    override fun equals(other: Any?): Boolean {
        if (other !is PArray) return false
        if (getPValue() == other.getPValue())
            return true
        return false
    }

    override fun hashCode(): Int = getPValue().hashCode()

    override fun checkIndexType(index: Variable): Boolean {
        return index is PInt
    }

    companion object {
        fun initializeArrayProperties() {
            val p = PArray(mutableListOf(), null)
            setProperty(p, "size") { pr: Primitive -> (pr as PArray).getPValue().size.toProperty() }
        }

        fun initializeEmbeddedArrayFunctions() {
            val p = PArray(mutableListOf(), null)
            setFunction(
                p,
                EmbeddedFunction(
                    "add", listOf("element"), listOf("index = this.size")
                ) { token, args ->
                    val list = castToArray(args.getPropertyOrNull("this")!!)
                    val argument = getIdent(token, "element", args)
                    val index = getInt(token, "index", args)
                    if (index.getPValue() < 0 || index.getPValue() > list.getPValue().size)
                        throw PositionalException("Index out of bounds", token.children[1])
                    list.getPValue().add(index.getPValue(), argument)
                }
            )
            setFunction(
                p,
                EmbeddedFunction(
                    "remove", args = listOf("element"),
                ) { token, args ->
                    val list = castToArray(args.getPropertyOrNull("this")!!)
                    val argument = getIdent(token, "element", args)
                    if (argument is Primitive) {
                        var removed = false
                        for (e in list.getPValue()) {
                            if (e is Primitive && e == argument) {
                                removed = true
                                list.getPValue().remove(e)
                                break
                            }
                        }
                        removed.toInt()
                    } else list.getPValue().remove(argument).toInt()
                }
            )
            setFunction(
                p,
                EmbeddedFunction("removeAt", listOf("index")) { token, args ->
                    val list = castToArray(args.getPropertyOrNull("this")!!)
                    val index = getInt(token, "index", args)
                    try {
                        list.getPValue().removeAt(index.getPValue())
                    } catch (e: IndexOutOfBoundsException) {
                        throw PositionalException(
                            "index ${index.getPValue()} out of bounds for length ${list.getPValue().size}"
                        )
                    }
                }
            )
            setFunction(
                p,
                EmbeddedFunction("has", listOf("element")) { token, args ->
                    val list = castToArray(args.getPropertyOrNull("this")!!)
                    val element = getIdent(token, "element", args)
                    if (element is Primitive)
                        list.getPValue().any { (it is Primitive && it == element) }.toInt()
                    else list.getPValue().any { it == element }.toInt()
                }
            )
            setFunction(
                p,
                EmbeddedFunction("joinToString", namedArgs = listOf("separator = \", \"")) { token, args ->
                    val array = getArray(token, "this", args)
                    val separator = getString(token, "separator", args)
                    array.getPValue().joinToString(separator = separator.getPValue())
                }
            )
            setFunction(
                p,
                EmbeddedFunction("clear") { _, args ->
                    val list = castToArray(args.getPropertyOrNull("this")!!)
                    list.getPValue().clear()
                }
            )
            setFunction(
                p,
                EmbeddedFunction("sort", listOf(), listOf("desc = false")) { token, args ->
                    val array = getArray(token, "this", args)
                    val desc = getInt(token, "desc", args)
                    val comparator = Comparator<Variable> { a, b -> compareVariables(a, b) }
                    array.getPValue().sortWith(comparator)
                    if (desc.getPValue() != 0)
                        array.getPValue().reverse()
                }
            )
            setFunction(p,
                EmbeddedFunction("sorted", namedArgs = listOf("reverse = false")) { token, args ->
                    val array = getArray(token, "this", args)
                    val desc = getInt(token, "reverse", args)
                    val comparator = Comparator<Variable> { a, b -> compareVariables(a, b) }
                    val res = array.getPValue().sortedWith(comparator)
                    if (desc.getPValue() != 0) res.reversed() else res
                }
            )
        }

        private fun compareVariables(a: Variable, b: Variable): Int {
            return if (a is Type) compareType(a, b) else return comparePrimitive(a as Primitive, b)
        }

        private fun compareType(type: Type, variable: Variable): Int {
            if (type is Object && variable is Object)
                return compareValues(type.name, variable.name)
            return when (variable) {
                is Object -> -1
                is Type -> if (type.equalToType(variable)) 0 else compareValues(type.name, variable.name)
                else -> 1
            }
        }

        private fun comparePrimitive(primitive: Primitive, variable: Variable): Int {
            if (variable is Type)
                return -1
            if (primitive is PNumber && variable is PNumber)
                return compareValues(primitive.getPValue().toDouble(), variable.getPValue().toDouble())
            if (primitive.getIndex() != (variable as Primitive).getIndex())
                return compareValues(primitive.getIndex(), variable.getIndex())
            return when (variable) {
                is PArray -> compareValues((primitive as PArray).getPValue().size, variable.getPValue().size)
                is PDictionary -> compareValues((primitive as PDictionary).getPValue().size, variable.getPValue().size)
                is PString -> compareValues((primitive as PString).getPValue(), variable.getPValue())
                else -> throw PositionalException("Non-comparable primitive")
            }
        }
    }
}

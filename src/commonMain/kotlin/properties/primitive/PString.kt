package properties.primitive

import References
import evaluation.FunctionFactory.getInt
import evaluation.FunctionFactory.getString
import isInt
import lexer.PositionalException
import node.Node
import properties.EmbeddedFunction
import properties.Type
import properties.Variable
import utils.Utils.castToString
import utils.Utils.toProperty

class PString(value: String, parent: Type? = null) : Primitive(value, parent), Indexable {
    override fun getIndex() = 4
    override fun getPValue() = value as String
    override fun get(index: Any, node: Node): Any {
        if (!isInt(index))
            throw PositionalException("Expected integer", node)
        if ((index as Int) < 0 || index >= getPValue().length)
            throw PositionalException("Index out of bounds", node)
        return getPValue()[index]
    }
    override fun toDebugClass(references: References): Any {
        return Pair("String", getPValue())
    }

    override fun set(index: Any, value: Any, nodeIndex: Node, nodeValue: Node) {
        throw PositionalException("Set is not implemented for String", nodeValue)
    }

    override fun toString(): String {
        return value.toString()
    }

    override fun checkIndexType(index: Variable): Boolean {
        return index is PInt
    }

    companion object {
        fun initializeStringProperties() {
            val s = PString("", null)
            setProperty(s, "size") { p: Primitive -> (p as PString).getPValue().length.toProperty() }
        }

        /**
         * * substring(start, end=this.size)
         * * replace(oldString, newString)
         * * reversed
         * * lowercase
         * * uppercase
         * * toArray: "abc" -> ["a", "b", "c"]
         */
        fun initializeEmbeddedStringFunctions() {
            val s = PString("", null)
            setFunction(
                s,
                EmbeddedFunction("substring", listOf("start"), listOf("end = this.size")) { token, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    val start = getInt(token, "start", args)
                    val end = getInt(token, "end", args)
                    string.getPValue().substring(start.getPValue(), end.getPValue())
                }
            )
            setFunction(
                s,
                EmbeddedFunction("replace", listOf("oldString", "newString")) { token, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    val oldString = getString(token, "oldString", args)
                    val newString = getString(token, "newString", args)
                    string.getPValue().replace(oldString.getPValue(), newString.getPValue())
                }
            )
            setFunction(
                s,
                EmbeddedFunction("reversed") { _, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    string.getPValue().reversed()
                }
            )
            setFunction(
                s,
                EmbeddedFunction("lowercase") { _, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    string.getPValue().lowercase()
                }
            )
            setFunction(
                s,
                EmbeddedFunction("uppercase") { _, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    string.getPValue().uppercase()
                }
            )
            setFunction(
                s,
                EmbeddedFunction("toArray") { _, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    string.getPValue().toCharArray().map { it.toString() }
                }
            )
        }
    }
}

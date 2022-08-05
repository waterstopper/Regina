package properties.primitive

import evaluation.FunctionFactory.getInt
import evaluation.FunctionFactory.getString
import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Type
import properties.Variable
import token.Token
import utils.Utils.castToString
import utils.Utils.parseAssignment
import utils.Utils.toProperty

class PString(value: String, parent: Type? = null) : Primitive(value, parent), Indexable {
    override fun getIndex() = 4
    override fun getPValue() = value as String
    override fun get(index: Any, token: Token): Any {
        if (index !is Int)
            throw PositionalException("Expected integer", token)
        if (index < 0 || index >= getPValue().length)
            throw PositionalException("Index out of bounds", token)
        return getPValue()[index]
    }

    override fun set(index: Any, value: Any, tokenIndex: Token, tokenValue: Token) {
        throw PositionalException("Set is not implemented for String", tokenValue)
    }

    override fun toString(): String {
        return "\"$value\""
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
                EmbeddedFunction(
                    "substring",
                    listOf(Token(value = "start")),
                    listOf(parseAssignment("end = this.size"))
                ) { token, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    val start = getInt(token, "start", args)
                    val end = getInt(token, "end", args)
                    string.getPValue().substring(start.getPValue(), end.getPValue())
                }
            )
            setFunction(
                s,
                EmbeddedFunction(
                    "replace",
                    listOf(Token(value = "oldString"), Token(value = "newString"))
                ) { token, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    val oldString = getString(token, "oldString", args)
                    val newString = getString(token, "newString", args)
                    string.getPValue().replace(oldString.getPValue(), newString.getPValue())
                }
            )
            setFunction(
                s,
                EmbeddedFunction("reversed", listOf()) { token, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    string.getPValue().reversed()
                }
            )
            setFunction(
                s,
                EmbeddedFunction("lowercase", listOf()) { token, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    string.getPValue().lowercase()
                }
            )
            setFunction(
                s,
                EmbeddedFunction("uppercase", listOf()) { token, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    string.getPValue().uppercase()
                }
            )
            setFunction(
                s,
                EmbeddedFunction("toArray", listOf()) { _, args ->
                    val string = castToString(args.getPropertyOrNull("this")!!)
                    string.getPValue().toCharArray().map { it.toString() }
                }
            )
        }
    }
}

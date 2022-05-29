package properties.primitive

import evaluation.FunctionFactory
import evaluation.FunctionFactory.getIdent
import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Type
import token.Token
import token.statement.Assignment
import utils.Utils.toProperty

class PString(value: String, parent: Type?) : Primitive(value, parent), Indexable {
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
            setFunction(s, EmbeddedFunction(
                "substring",
                listOf(Token(value = "start")),
                listOf(Assignment(value = "end = this.size"))
            )
            { token, args ->
                val string = args.getPropertyOrNull("this")!!
                val start = getIdent(token, "start", args)
                if (string !is PString)
                    throw PositionalException("Expected string", token)
                if (start !is PInt)
                    throw PositionalException("Expected number as argument", token)
                val end = getIdent(token, "end", args)
                if (end !is PInt)
                    throw PositionalException("Expected number as second argument", token)
                string.getPValue().substring(start.getPValue(), end.getPValue())
            })
            setFunction(s, EmbeddedFunction("replace",
                listOf(Token(value="oldString"), Token(value="newString")))
            { token, args ->
                val string = args.getPropertyOrNull("this")!!
                val oldString = getIdent(token, "oldString", args)
                val newString = getIdent(token, "newString", args)
                if (string is PString && oldString is PString && newString is PString)
                    string.getPValue().replace(oldString.getPValue(), newString.getPValue())
                else throw PositionalException("Expected string", token)
            })
            setFunction(s, EmbeddedFunction("reversed", listOf()) { token, args ->
                val string = args.getPropertyOrNull("this")!!
                if (string is PString)
                    string.getPValue().reversed()
                else throw PositionalException("Expected string", token)
            })
            setFunction(s, EmbeddedFunction("lowercase", listOf()) { token, args ->
                val string = args.getPropertyOrNull("this")!!
                if (string is PString)
                    string.getPValue().lowercase()
                else throw PositionalException("Expected string", token)
            })
            setFunction(s, EmbeddedFunction("uppercase", listOf()) { token, args ->
                val string = args.getPropertyOrNull("this")!!
                if (string is PString)
                    string.getPValue().uppercase()
                else throw PositionalException("Expected string", token)
            })
            setFunction(s, EmbeddedFunction("toArray", listOf()) { token, args ->
                val string = args.getPropertyOrNull("this")!!
                if (string is PString)
                    string.getPValue().toCharArray().map { it.toString() }
                else throw PositionalException("Expected string", token)
            })
        }
    }
}
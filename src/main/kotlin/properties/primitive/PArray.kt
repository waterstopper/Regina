package properties.primitive

import evaluation.FunctionFactory.getIdent
import lexer.Parser
import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Type
import properties.Variable
import token.Token
import token.statement.Assignment
import utils.Utils.toInt
import utils.Utils.toProperty
import utils.Utils.toVariable

class PArray(value: MutableList<Variable>, parent: Type?) : Primitive(value, parent), Indexable {
    override fun getIndex() = 5
    override fun getPValue() = value as MutableList<Variable>
    override fun get(index: Any, token: Token): Any {
        if (index !is Int)
            throw PositionalException("Expected integer", token)
        if (index < 0 || index >= getPValue().size)
            throw PositionalException("Index out of bounds", token)
        return getPValue()[index]
    }

    override fun set(index: Any, value: Any, tokenIndex: Token, tokenValue: Token) {
        getPValue()[index as Int] = value.toVariable(tokenIndex)
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

    companion object {
        fun initializeArrayProperties() {
            val p = PArray(mutableListOf(), null)
            setProperty(p, "size") { pr: Primitive -> (pr as PArray).getPValue().size.toProperty() }
        }

        fun initializeEmbeddedArrayFunctions() {
            val p = PArray(mutableListOf(), null)
            setFunction(p, EmbeddedFunction(
                "add",
                listOf(Token(value = "element")),
                listOf(Parser("index = this.size").statements().first() as Assignment)
            ) { token, args ->
                val list = args.getPropertyOrNull("this")!!
                if (list is PArray) {
                    val argument = getIdent(token, "element", args)
                    val index = getIdent(token, "index", args)
                    if (index !is PInt) throw PositionalException("expected integer as index", token.children[2])
                    if (index.getPValue() < 0 || index.getPValue() > list.getPValue().size)
                        throw PositionalException("Index out of bounds", token.children[1])
                    list.getPValue().add(index.getPValue(), argument)
                } else throw PositionalException("add is not applicable for this type", token.children[1])
            })
            setFunction(p, EmbeddedFunction(
                "remove",
                listOf(Token(value = "element")),
            ) { token, args ->
                val list = args.getPropertyOrNull("this")!!
                if (list is PArray) {
                    val argument = getIdent(token, "element", args)
                    if (argument is Primitive) {
                        var removed = false
                        for (e in (list.value as MutableList<*>)) {
                            if (e is Primitive && e == argument) {
                                removed = true
                                (list.value as MutableList<*>).remove(e)
                                break
                            }
                        }
                        removed.toInt()
                    } else (list.value as MutableList<*>).remove(argument).toInt()
                } else throw PositionalException("remove is not applicable for this type", token.children[1])
            })
            setFunction(p, EmbeddedFunction("removeAt", listOf(Token(value = "index"))) { token, args ->
                val list = args.getPropertyOrNull("this")!!
                val index = getIdent(token, "index", args)
                if (list is PArray) {
                    if (index is PInt)
                        try {
                            (list.value as MutableList<*>).removeAt(index.getPValue())!!
                        } catch (e: IndexOutOfBoundsException) {
                            throw PositionalException("index ${index.getPValue()} out of bounds for length ${(list.value as MutableList<*>).size}")
                        }
                    else throw PositionalException("expected integer as index", token.children[2])
                } else throw PositionalException("removeAt is not applicable for this type", token.children[1])
            })
            setFunction(p, EmbeddedFunction("has", listOf(Token(value = "element"))) { token, args ->
                val list = args.getPropertyOrNull("this")!!
                val element = getIdent(token, "element", args)
                if (list is PArray) {
                    if (element is Primitive)
                        (list.value as MutableList<*>).any { (it is Primitive && it == element) }.toInt()
                    else (list.value as MutableList<*>).any { it == element }.toInt()
                } else throw PositionalException("has is not applicable for this type", token.children[1])
            })
            setFunction(
                p, EmbeddedFunction(
                    "joinToString", listOf(),
                    listOf(Parser("separator = \",\"").statements().first() as Assignment)
                ) { token, args ->
                    val array = getIdent(token, "this", args)
                    val separator = getIdent(token, "separator", args)
                    if (array !is PArray)
                        throw PositionalException("joinToString is not applicable for this type", token.children[1])
                    if (separator !is PString)
                        throw PositionalException("joinToString should have String as separator", token.children[1])
                    array.getPValue().joinToString(separator = separator.getPValue())
                })
        }
    }

}

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
            setFunction(
                p,
                EmbeddedFunction(
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
                }
            )
            setFunction(
                p,
                EmbeddedFunction(
                    "remove",
                    listOf(Token(value = "element")),
                ) { token, args ->
                    val list = args.getPropertyOrNull("this")!!
                    if (list is PArray) {
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
                    } else throw PositionalException("remove is not applicable for this type", token.children[1])
                }
            )
            setFunction(
                p,
                EmbeddedFunction("removeAt", listOf(Token(value = "index"))) { token, args ->
                    val list = args.getPropertyOrNull("this")!!
                    val index = getIdent(token, "index", args)
                    if (list is PArray) {
                        if (index is PInt)
                            try {
                                list.getPValue().removeAt(index.getPValue())
                            } catch (e: IndexOutOfBoundsException) {
                                throw PositionalException("index ${index.getPValue()} out of bounds for length ${list.getPValue().size}")
                            }
                        else throw PositionalException("expected integer as index", token.children[2])
                    } else throw PositionalException("removeAt is not applicable for this type", token.children[1])
                }
            )
            setFunction(
                p,
                EmbeddedFunction("has", listOf(Token(value = "element"))) { token, args ->
                    val list = args.getPropertyOrNull("this")!!
                    val element = getIdent(token, "element", args)
                    if (list is PArray) {
                        if (element is Primitive)
                            list.getPValue().any { (it is Primitive && it == element) }.toInt()
                        else list.getPValue().any { it == element }.toInt()
                    } else throw PositionalException("has is not applicable for this type", token.children[1])
                }
            )
            setFunction(
                p,
                EmbeddedFunction(
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
                }
            )
            setFunction(
                p,
                EmbeddedFunction(
                    "sort", listOf(),
                    listOf(Parser("desc = false").statements().first() as Assignment)
                ) { token, args ->
                    val array = getIdent(token, "this", args)
                    val desc = getIdent(token, "desc", args)
                    if (array !is PArray)
                        throw PositionalException("sort is not applicable for this type", token.children[1])
                    if (desc !is PInt)
                        throw PositionalException("sort should have Int as desc value", token.children[1])
                    val comparator = Comparator<Variable> { a, b -> compareVariables(a, b) }
                    array.getPValue().sortWith(comparator)
                    if (desc.getPValue() != 0)
                        array.getPValue().reverse()
                }
            )
            setFunction(
                p,
                EmbeddedFunction(
                    "sorted", listOf(),
                    listOf(Parser("desc = false").statements().first() as Assignment)
                ) { token, args ->
                    val array = getIdent(token, "this", args)
                    val desc = getIdent(token, "desc", args)
                    if (array !is PArray)
                        throw PositionalException("sort is not applicable for this type", token.children[1])
                    if (desc !is PInt)
                        throw PositionalException("sort should have Int as desc value", token.children[1])
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
            return when (variable) {
                is Type -> if (type.equalToType(variable)) 0 else (type.name > variable.name).toInt()
                else -> 1
            }
        }

        // TODO compare by index
        private fun comparePrimitive(primitive: Primitive, variable: Variable): Int {
            return when (variable) {
                is Type -> 0
                is PArray, is PDictionary -> {
                    if (primitive is PArray || primitive is PDictionary) {
                        0
                    } else 0
                }
                else -> 0
            }
        }
    }
}

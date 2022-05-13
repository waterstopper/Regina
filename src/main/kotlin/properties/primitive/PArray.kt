package properties.primitive

import lexer.PositionalException
import properties.EmbeddedFunction
import properties.Type
import properties.Variable
import token.Token
import utils.Utils.toInt

class PArray(value: MutableList<Variable>, parent: Type?) : Primitive(value, parent) {
    override fun getIndex() = 0
    override fun getPValue() = value as MutableList<Variable>

    override fun toString(): String {
        val res = StringBuilder("[")
        for (e in getPValue())
            res.append("${if (e == this) "this" else e.toString()}, ")
        return res.removeRange(res.lastIndex - 1..res.lastIndex).toString() + ']'
    }

    fun getByIndex(token: Token, index: Int): Variable {
        checkBounds(token, index)
        return (value as MutableList<*>)[index]!! as Variable
    }

    private fun checkBounds(token: Token, index: Int) {
        if (index > (value as MutableList<*>).lastIndex)
            throw PositionalException(
                "index $index out of bounds for array of size ${(value as MutableList<*>).size}",
                token
            )
    }

    override fun equals(other: Any?): Boolean {
        if (other !is PArray) return false
        if (getPValue() == other.getPValue())
            return true
        return false
    }

    override fun hashCode(): Int = getPValue().hashCode()

    companion object {
        fun initializeEmbeddedArrayFunctions() {
            val p = PArray(mutableListOf(), null)
            setFunction(p, EmbeddedFunction("add", listOf("i", "x"), { token, args ->
                val list = args.getVariable("(this)")
                if (list is PArray) {
                    val argument =
                        if (args.getVariableOrNull("x") != null) args.getVariable("x") else args.getVariable("i")
                    val indexVar: Any = args.getVariable("i")
                    var index = (list.value as MutableList<*>).size
                    if (args.getVariableOrNull("x") != null)
                        if (indexVar is Primitive && indexVar.getPValue() is Int) {
                            index = (indexVar.getPValue() as Int)
                        } else throw PositionalException("expected integer as index", token.children[2])
                    (list.value as MutableList<Any>).add(index, argument)
                } else throw PositionalException("add is not applicable for this type", token.children[1])
            }, 1..2))
            setFunction(p, EmbeddedFunction("remove", listOf("x"), { token, args ->
                val list = args.getVariable("(this)")
                if (list is PArray) {
                    val argument = args.getVariable("x")
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
            }, 1..1))
            setFunction(p, EmbeddedFunction("removeAt", listOf("i"), { token, args ->
                val list = args.getVariable("(this)")
                val index = args.getVariable("i")
                if (list is PArray) {
                    if (index is PInt)
                        try {
                            (list.value as MutableList<*>).removeAt(index.getPValue())!!
                        } catch (e: IndexOutOfBoundsException) {
                            throw PositionalException("index ${index.getPValue()} out of bounds for length ${(list.value as MutableList<*>).size}")
                        }
                    else throw PositionalException("expected integer as index", token.children[2])
                } else throw PositionalException("removeAt is not applicable for this type", token.children[1])
            }, 1..1))
            setFunction(p, EmbeddedFunction("has", listOf("x"), { token, args ->
                val list = args.getVariable("(this)")
                val element = args.getVariable("x")
                if (list is PArray) {
                    if (element is Primitive)
                        (list.value as MutableList<*>).any { (it is Primitive && it == element) }.toInt()
                    else (list.value as MutableList<*>).any { it == element }.toInt()
                } else throw PositionalException("has is not applicable for this type", token.children[1])
            }, 1..1))
        }
    }

}

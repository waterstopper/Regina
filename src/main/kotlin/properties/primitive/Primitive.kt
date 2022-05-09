package properties.primitive

import lexer.PositionalException
import properties.*
import properties.Function
import token.Token

/**
 * Stores Array, String, Int, Double values
 */
abstract class Primitive(protected open var value: Any, parent: Type?) : Property(parent) {
    open fun getIndex() = -1
    open fun getPValue() = value
    override fun toString() = "$value"

    fun getSymbol(): String {
        return when (value) {
            is Number -> "(NUMBER)"
            is String -> "(STRING)"
            is MutableList<*> -> "[]"
            else -> throw Exception("unsupported type")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Primitive) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun getPropertyOrNull(name: String) = properties[getIndex()][name]
    override fun getProperty(token: Token) =
        properties[getIndex()][token.value] ?: throw PositionalException("`${token.value}` not found", token)


    override fun getFunction(token: Token): Function =
        functions[getIndex()].find { it.name == token.value }
            ?: throw PositionalException("Primitive does not contain `${token.value}` function", token)

    override fun getFunctionOrNull(name: String) = functions[getIndex()].find { it.name == name }
//    private fun getPrimitiveIndex(): Int {
//        return when (this) {
//            is PArray -> 0
//            is PDouble -> 1
//            is PInt -> 2
//            is PString -> 3
//            else -> 4
//        }
//    }

    companion object {
        fun setProperty(primitive: Primitive, name: String, property: Property) {
            properties[primitive.getIndex()][name] = property
        }

        fun setFunction(primitive: Primitive, embeddedFunction: EmbeddedFunction) {
            functions[primitive.getIndex()].add(embeddedFunction)
        }

        val properties = List(4) { mutableMapOf<String, Property>() }
        val functions = List(4) { mutableListOf<Function>() }
        fun createPrimitive(value: Any, parent: Type? = null, token: Token = Token()): Primitive {
            return when (value) {
                is String -> PString(value, parent)
                is List<*> -> PArray(value as MutableList<Variable>, parent)
                is Int -> PInt(value, parent)
                is Double -> PDouble(value, parent)
                else -> throw PositionalException("cannot create variable of type `${value::class}`", token)
            }
        }
    }
}
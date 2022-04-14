package properties.primitive

import lexer.PositionalException
import lexer.Token
import properties.Property
import structure.SymbolTable.Type

/**
 * Stores Array, String, Int, Double values
 */
abstract class Primitive(name: String, var value: Any, parent: Type?) : Property(name, parent) {

    fun getSymbol(): String {
        return when (value) {
            is Number -> "(NUMBER)"
            is String -> "(STRING)"
            is MutableList<*> -> "[]"
            else -> throw Exception("unsupported type")
        }
    }

    override fun toString() = "$value"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Primitive) return false
        if (!super.equals(other)) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    companion object {
        fun createPrimitive(value: Any, parent: Type?, token: Token=Token()): Primitive {
            return when (value) {
                is String -> PString(value, parent)
                is List<*> -> PArray(value as MutableList<Any>, parent)
                is Int -> PInt(value, parent)
                is Double -> PDouble(value, parent)
                else -> throw PositionalException("cannot create variable of type ${value.javaClass}", token)
            }
        }
    }
}
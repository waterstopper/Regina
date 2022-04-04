package properties

/**
 * Stores Array, String, Int, Double values
 */
class Primitive(name: String, var value: Any, parent: Type?) : Property(name, parent) {

    fun getSymbol(): String {
        return when (value) {
            is Number -> "(NUMBER)"
            is String -> "(STRING)"
            is MutableList<*> -> "[]"
            else -> throw Exception("unsupported type")
        }
    }

    override fun toString() = "$value"
}
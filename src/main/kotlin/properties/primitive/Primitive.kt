package properties.primitive

import lexer.NotFoundException
import lexer.PositionalException
import lexer.RuntimeError
import delete.Delete
import properties.*
import properties.Function
import node.Node
import node.invocation.Call

/**
 * Stores Dictionary, Array, String, Int, Double values.
 *
 * **Array and Dictionary are mutable**, unlike other primitive classes
 */
abstract class Primitive(protected open var value: Any, parent: Type?) : Property(parent) {
    open fun getIndex() = 0
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
        // var result = super.hashCode()
        //   result = 31 * result + value.hashCode()
        return value.hashCode()
    }

    override fun getPropertyOrNull(name: String) = when (name) {
        "this" -> this
        "parent" -> getParentOrNull()
        "properties" -> getProperties()
        else -> properties[getIndex()][name]?.let { it(this) }
            ?: (if (getIndex() in 2..3)
                properties[1][name]?.let { it(this) } ?: properties[0][name]?.let { it(this) }
            else properties[0][name]?.let { it(this) })
    }

    override fun getProperty(node: Node): Property = when (node.value) {
        "this" -> this
        "parent" -> getParentOrNull()
        "properties" -> getProperties()
        else -> properties[getIndex()][node.value]?.let { it(this) }
            ?: (if (getIndex() in 2..3)
                properties[1][node.value]?.let { it(this) } ?: properties[0][node.value]?.let { it(this) }
            else properties[0][node.value]?.let { it(this) })
            ?: throw NotFoundException(node, variable = this)
    }

    /**
     * Does not include "this"
     */
    override fun getProperties(): PDictionary {
        val res = properties[0]
        if (getIndex() in 2..3)
            res.putAll(properties[1])
        res.putAll(properties[getIndex()])
        return PDictionary(res.mapValues { it.value(this) }.toMutableMap(), null)
    }

    override fun getFunction(node: Node): Function = getFunctionOrNull(node)
        ?: throw PositionalException("${formatClassName()} does not contain function", node)

    override fun getFunctionOrNull(node: Node): Function? = Function.getFunctionOrNull(
        node as Call,
        if (getIndex() in 2..3)
            (functions[getIndex()] + functions[1]) + functions[0] else functions[getIndex()]
    )

    private fun formatClassName() = this::class.toString().split('.').last().substring(1)

    companion object {
        fun setProperty(primitive: Primitive, name: String, property: (s: Primitive) -> Property) {
            properties[primitive.getIndex()][name] = property
        }

        fun setFunction(primitive: Primitive, embeddedFunction: EmbeddedFunction) {
            functions[primitive.getIndex()].add(embeddedFunction)
        }

        val properties = List(7) { mutableMapOf<String, (s: Primitive) -> Property>() }
        val functions = List(7) { mutableSetOf<Function>() }
        fun createPrimitive(value: Any, parent: Type? = null, node: Node = Node()): Primitive {
            return when (value) {
                is String -> PString(value, parent)
                is List<*> -> PArray(value as MutableList<Variable>, parent)
                is Int -> PInt(value, parent)
                is Double -> PDouble(value, parent)
                is MutableMap<*, *> -> PDictionary(value as MutableMap<out Any, out Variable>, parent)
                else -> throw PositionalException(
                    "Cannot create variable of type `${value::class}`", node
                )
            }
        }

        fun createPrimitive(value: Any, parent: Type? = null, delete: Delete): Primitive {
            return when (value) {
                is String -> PString(value, parent)
                is List<*> -> PArray(value as MutableList<Variable>, parent)
                is Int -> PInt(value, parent)
                is Double -> PDouble(value, parent)
                is MutableMap<*, *> -> PDictionary(value as MutableMap<out Any, out Variable>, parent)
                else -> throw RuntimeError(
                    "Cannot create variable of type `${value::class}`", delete
                )
            }
        }
    }
}

package utils

import delete.Delete
import lexer.Parser
import lexer.PositionalException
import node.Node
import node.statement.Assignment
import properties.Property
import properties.Type
import properties.Variable
import properties.primitive.*

object Utils {
    init {
        println("INI")
        PArray.initializeEmbeddedArrayFunctions()
        PString.initializeEmbeddedStringFunctions()
        PNumber.initializeEmbeddedNumberFunctions()
        PDouble.initializeEmbeddedDoubleFunctions()
        PDictionary.initializeDictionaryFunctions()

        PInt.initializeIntProperties()
        PDouble.initializeDoubleProperties()
        PArray.initializeArrayProperties()
        PString.initializeStringProperties()
        PDictionary.initializeDictionaryProperties()
    }

    fun Boolean.toInt(): Int = if (this) 1 else 0
    fun Boolean.toNonZeroInt(): Int = if (this) 1 else -1

    fun Any.toBoolean(node: Node): Boolean {
        try {
            return this.toString().toDouble() != 0.0
        } catch (e: NumberFormatException) {
            throw PositionalException("expected numeric value", node)
        }
    }

    fun Any.toVariable(node: Node = Node()): Variable =
        if (this is Variable) this else Primitive.createPrimitive(this, null, node)

    fun Any.toProperty(node: Node = Node(), parent: Type? = null): Property =
        if (this is Property) this else Primitive.createPrimitive(this, parent, node)

    fun Any.toVariable(delete: Delete): Variable =
        if (this is Variable) this else Primitive.createPrimitive(this, null, delete)

    fun Any.toProperty(delete: Delete, parent: Type? = null): Property =
        if (this is Property) this else Primitive.createPrimitive(this, parent, delete)

    fun parseAssignment(assignment: String) = Parser(assignment).statements().first().toNode() as Assignment

    /**
     * Prints AST with indentation to  show children.
     * **For debug**.
     */
    fun List<Node>.treeView(): String {
        val res = StringBuilder()
        for (t in this) {
            res.append(t.toTreeString(0))
            res.append('\n')
        }
        return res.toString()
    }

    fun unifyPNumbers(first: Variable, second: Variable, node: Node): List<Number> {
        val firstNumber =
            if (first is PNumber) first.getPValue() else throw PositionalException("Expected number", node)
        val secondNumber =
            if (second is PNumber) second.getPValue() else throw PositionalException("Expected number", node)
        return unifyNumbers(firstNumber, secondNumber, node)
    }

    fun unifyNumbers(first: Any, second: Any, node: Node): List<Number> {
        if (first !is Number)
            throw PositionalException("left operand is not numeric for this infix operator", node)
        if (second !is Number)
            throw PositionalException("right operand is not numeric for this infix operator", node)
        if (first is Int && second is Int)
            return listOf(first, second)
        return listOf(first.toDouble(), second.toDouble())
    }

    operator fun Number.unaryMinus(): Any {
        return if (this is Double)
            -this
        else -this.toInt()
    }

    fun <T> List<T>.subList(start: Int): List<T> = this.subList(start, this.size)

    fun castToArray(array: Any): PArray {
        if (array !is PArray)
            throw PositionalException("function in not applicable for this type")
        return array
    }

    fun castToString(str: Any): PString {
        if (str !is PString)
            throw PositionalException("function in not applicable for this type")
        return str
    }

    fun castToInt(int: Any): PInt {
        if (int !is PInt)
            throw PositionalException("function in not applicable for this type")
        return int
    }

    fun castToNumber(num: Any): PNumber {
        if (num !is PNumber)
            throw PositionalException("Expected number")
        return num
    }
}

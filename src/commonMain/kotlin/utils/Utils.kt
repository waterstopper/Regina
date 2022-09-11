package utils

import isDouble
import isInt
import lexer.Parser
import lexer.PositionalException
import node.Identifier
import node.Node
import node.invocation.Invocation
import node.operator.Index
import node.statement.Assignment
import properties.Property
import properties.RFunction
import properties.Type
import properties.Variable
import properties.primitive.*
import table.FileTable
import kotlin.reflect.KClass

object Utils {
    val NULL = PInt(0, null)

    init {
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

    fun Any.toBoolean(node: Node, fileTable: FileTable): Boolean {
        try {
            return this.toString().toDouble() != 0.0
        } catch (e: NumberFormatException) {
            throw PositionalException("expected numeric value", fileTable.filePath, node)
        }
    }

    fun Any.toVariable(node: Node = Node()): Variable =
        if (this is Variable) this else Primitive.createPrimitive(this, null, node)

    fun Any.toProperty(node: Node = Node(), parent: Type? = null): Property =
        if (this is Property) this else Primitive.createPrimitive(this, parent, node)

    fun parseAssignment(assignment: String) =
        Parser(assignment, "@NoFile").statements().first().toNode("@NoFile") as Assignment

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

    fun unifyPNumbers(first: Variable, second: Variable, node: Node, filePath: String): List<Number> {
        val firstNumber =
            if (first is PNumber) first.getPValue() else throw PositionalException("Expected number", filePath, node)
        val secondNumber =
            if (second is PNumber) second.getPValue() else throw PositionalException("Expected number", filePath, node)
        return unifyNumbers(firstNumber, secondNumber, node, filePath)
    }

    fun unifyNumbers(first: Any, second: Any, node: Node, filePath: String): List<Number> {
        if (first !is Number)
            throw PositionalException("left operand is not numeric for this infix operator", filePath, node)
        if (second !is Number)
            throw PositionalException("right operand is not numeric for this infix operator", filePath, node)
        if (isInt(first) && isInt(second))
            return listOf(first, second)
        return listOf(first.toDouble(), second.toDouble())
    }

    operator fun Number.unaryMinus(): Any {
        return if (isDouble(this))
            -(this as Double)
        else -this.toInt()
    }

    fun <T> List<T>.subList(start: Int): List<T> = this.subList(start, this.size)

    fun castToArray(array: Any): PArray {
        return array as PArray
    }

    fun castToString(str: Any): PString {
        return str as PString
    }

    fun castToNumber(num: Any): PNumber {
        return num as PNumber
    }

    fun mapToString(mapped: KClass<*>): String {
        return when (mapped) {
            RFunction::class -> "Function"
            PInt::class -> "Int"
            PDouble::class -> "Double"
            PNumber::class -> "Number"
            PString::class -> "String"
            PArray::class -> "Array"
            PDictionary::class -> "Dictionary"
            Identifier::class -> "Identifier"
            Invocation::class -> "Invocation"
            Index::class -> "Index"
            else -> mapped.toString().split(".").last()
        }
    }
}

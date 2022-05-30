package utils

import lexer.PositionalException
import properties.Property
import properties.Type
import properties.Variable
import properties.primitive.*
import properties.primitive.PDictionary.Companion.initializeDictionaryProperties
import table.SymbolTable.Companion.initializeObjects
import token.Token

object Utils {
    init {
        PArray.initializeEmbeddedArrayFunctions()
        PString.initializeEmbeddedStringFunctions()
        PNumber.initializeEmbeddedNumberFunctions()
        PDouble.initializeEmbeddedDoubleFunctions()

        PInt.initializeIntProperties()
        PDouble.initializeDoubleProperties()
        PArray.initializeArrayProperties()
        PString.initializeStringProperties()
        initializeDictionaryProperties()
    }

    fun Boolean.toInt(): Int = if (this) 1 else 0

    fun Any.toBoolean(token: Token): Boolean {
        try {
            return this.toString().toDouble() != 0.0
        } catch (e: NumberFormatException) {
            throw PositionalException("expected numeric value", token)
        }
    }

    fun Any.toVariable(token: Token): Variable =
        if (this is Variable) this else Primitive.createPrimitive(this, null, token)

    fun Any.toProperty(token: Token = Token(), parent: Type? = null): Property =
        if (this is Property) this else Primitive.createPrimitive(this, parent, token)

    /**
     * Prints AST with indentation to  show children.
     * **For debug**.
     */
    fun List<Token>.treeView(): String {
        val res = StringBuilder()
        for (t in this) {
            res.append(t.toTreeString(0))
            res.append('\n')
        }
        return res.toString()
    }

    fun unifyPNumbers(first: Variable, second: Variable, token: Token): List<Number> {
        val firstNumber =
            if (first is PNumber) first.getPValue() else throw PositionalException("Expected number", token)
        val secondNumber =
            if (second is PNumber) second.getPValue() else throw PositionalException("Expected number", token)
        return unifyNumbers(firstNumber, secondNumber, token)
    }

    fun unifyNumbers(first: Any, second: Any, token: Token): List<Number> {
        if (first !is Number)
            throw PositionalException("left operand is not numeric for this infix operator", token)
        if (second !is Number)
            throw PositionalException("right operand is not numeric for this infix operator", token)
        if (first is Int && second is Int)
            return listOf(first, second)
        return listOf(first.toDouble(), second.toDouble())
    }

    operator fun Number.unaryMinus(): Any {
        return if (this is Double)
            -this
        else -this.toInt()
    }
}
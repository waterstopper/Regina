package utils

import lexer.PositionalException
import token.Token

object Utils {
    fun Boolean.toInt(): Int = if (this) 1 else 0

    fun Any.toBoolean(token: Token): Boolean {
        try {
            return this.toString().toDouble() != 0.0
        } catch (e: NumberFormatException) {
            throw PositionalException("expected numeric value", token)
        }
    }

    fun List<Token>.treeView(): String {
        val res = StringBuilder()
        for (t in this) {
            res.append(t.toTreeString(0))
            res.append('\n')
        }
        return res.toString()
    }

    operator fun Number.unaryMinus(): Any {
        return if (this is Double)
            -this
        else -this.toInt()
    }
}
package evaluation

import evaluation.Evaluation.evaluateInvocation
import lexer.PositionalException
import lexer.Token
import properties.Primitive
import properties.Type
import structure.*

object ValueEvaluation {
    fun evaluateValue(token: Token, symbolTable: SymbolTable): Any {
        return when (token.symbol) {
            "." -> evaluateLink(token, symbolTable)
            "(IDENT)" -> evaluateIdentifier(token, symbolTable)
            "(NUMBER)" -> if (token.value.contains(".")) token.value.toDouble() else token.value.toInt()
            "(STRING)" -> token.value.substring(1, token.value.length - 1)
            "(" -> {
                val res = evaluateInvocation(token, symbolTable)
                if (res is Unit)
                    throw PositionalException("expected value but nothing was returned from function", token)
                res
            }
            "[" -> {
                val element = evaluateIndex(token, symbolTable)
                if (element is Primitive)
                    return element.value
                return element
            }
            "true" -> 1
            "false" -> 0
            "!" -> evaluateNot(token, symbolTable)
            "[]" -> token.children.map { evaluateValue(it, symbolTable) }.toMutableList()
            "if" -> evaluateTernary(token, symbolTable)
            "+" -> evaluateValue(token.children[0], symbolTable) + evaluateValue(token.children[1], symbolTable)
            else -> evaluateInfixArithmetic(token, symbolTable)
        }
    }

    fun evaluateLink(token: Token, symbolTable: SymbolTable): Any {
        var linkRoot = token
        var table = symbolTable
        while (linkRoot.value == ".") {
            val type = evaluateValue(linkRoot.children[0], table)
            if (type !is Type)
                throw PositionalException("expected class", linkRoot.children[0])
            linkRoot = linkRoot.children[1]
            table = type.symbolTable
        }
        return evaluateValue(linkRoot, table)
    }

    private fun evaluateIdentifier(token: Token, symbolTable: SymbolTable): Any {
        val identifier =
            symbolTable.findIndentfier(token.value) ?: TypeManager.find(token.value) ?: throw PositionalException(
                "no identifier named ${token.value}",
                token
            )
        return if (identifier is Primitive)
            identifier.value
        else identifier
    }

    fun evaluateIndex(token: Token, symbolTable: SymbolTable): Any {
        val array = evaluateValue(token.children[0], symbolTable)
        if (array is List<*>) {
            val index = evaluateValue(token.children[1], symbolTable)
            if (index is Int)
                return array[index]!!
            else throw PositionalException("expected Int as index", token)
        } else throw PositionalException("array expected", token)
    }

    private fun evaluateInfixArithmetic(token: Token, symbolTable: SymbolTable): Number {
        if (token.children.size == 1) {
            val a = evaluateValue(token.children[0], symbolTable)
            return when (token.symbol) {
                "-" -> evaluateUnaryMinus(a as Number)
                else -> throw PositionalException("no such prefix operator", token)
            }
        }
        if (token.children.size == 2) {
            val (a, b) = unifyNumbers(
                evaluateValue(token.children[0], symbolTable),
                evaluateValue(token.children[1], symbolTable),
                token
            )
            return when (token.symbol) {
                ">" -> (a.toDouble() > b.toDouble()).toInt()
                "<" -> (a.toDouble() < b.toDouble()).toInt()
                ">=" -> (a.toDouble() >= b.toDouble()).toInt()
                "<=" -> (a.toDouble() <= b.toDouble()).toInt()
                "==" -> (a == b).toInt()
                "!=" -> (a != b).toInt()
                "&" -> (a != 0 && b != 0).toInt()
                "|" -> (a != 0 || b != 0).toInt()
                "//" -> a.toInt() / b.toInt()
                else -> evaluateDuplicatedOperators(a, b, token)
            }
        } else throw PositionalException("expected infix operator", token)
    }

    private fun evaluateDuplicatedOperators(first: Number, second: Number, token: Token): Number {
        if (first is Double) {
            val a = first.toDouble()
            val b = second.toDouble()
            return when (token.symbol) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> a / b
                "%" -> a % b
                else -> throw PositionalException("operator ${token.symbol} not implemented", token)
            }
        } else {
            val a = first.toInt()
            val b = second.toInt()
            return when (token.symbol) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> a / b
                "%" -> a % b
                else -> throw PositionalException("operator ${token.symbol} not implemented", token)
            }
        }
    }

    private fun unifyNumbers(first: Any, second: Any, token: Token): List<Number> {
        if (first !is Number)
            throw PositionalException("left operand is not numeric for this infix operator", token)
        if (second !is Number)
            throw PositionalException("right operand is not numeric for this infix operator", token)
        if (first is Int && second is Int)
            return listOf(first, second)
        return listOf(first.toDouble(), second.toDouble())
    }

    private fun evaluateUnaryMinus(number: Number): Number = if (number is Double) -number else -(number as Int)

    private fun evaluateTernary(token: Token, symbolTable: SymbolTable): Any {
        if (token.children.size != 3)
            throw PositionalException("ternary if should have else branch", token)
        return if (evaluateValue(token.children[0], symbolTable) != 0)
            evaluateValue(token.children[1], symbolTable)
        else evaluateValue(token.children[2], symbolTable)
    }

    private operator fun Any.plus(other: Any): Any {
        if (this is MutableList<*>) {
            if (other is MutableList<*>)
                return (this as MutableList<Any>).addAll(other as MutableList<Any>)
            else (this as MutableList<Any>).add(other)
        }
        if (this is String || other is String)
            return this.toString() + other.toString()
        if (this is Double && other is Number || this is Number && other is Double)
            return this.toString().toDouble() + other.toString().toDouble()
        if (this is Int && other is Int)
            return this + other
        else throw Exception("operator not applicable to operands")
    }

    private fun evaluateNot(token: Token, symbolTable: SymbolTable): Int {
        val res = (evaluateValue(token.children[0], symbolTable))
        if (res is Number)
            return (res == 0).toInt()
        throw PositionalException("! operator applicable to numeric", token)
    }

    fun Boolean.toInt(): Int = if (this) 1 else 0
}



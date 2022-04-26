package token.operator

import lexer.Parser
import lexer.PositionalException
import table.SymbolTable
import token.Token
import utils.Utils.toInt

class ArithmeticOperator(
    symbol: String,
    value: String,
    position: Pair<Int, Int>,
    bindingPower: Int,
    nud: ((token: Token, parser: Parser) -> Token)?,
    led: ((
        token: Token, parser: Parser, token2: Token
    ) -> Token)?,
    std: ((token: Token, parser: Parser) -> Token)?
) : Operator(symbol, value, position, bindingPower, nud, led, std) {

    override fun evaluate(symbolTable: SymbolTable): Any {
        if (children.size == 1) {
            return when (symbol) {
                "-" -> evaluateUnaryMinus(left.evaluate(symbolTable) as Number)
                "!" -> evaluateNot(symbolTable)
                else -> throw PositionalException("no such prefix arithmetic operator", this)
            }
        }
        val (a, b) = unifyNumbers(left.evaluate(symbolTable), right.evaluate(symbolTable))
        return when (value) {
            ">" -> (a.toDouble() > b.toDouble()).toInt()
            "<" -> (a.toDouble() < b.toDouble()).toInt()
            ">=" -> (a.toDouble() >= b.toDouble()).toInt()
            "<=" -> (a.toDouble() <= b.toDouble()).toInt()
            "&" -> (a != 0 && b != 0).toInt()
            "|" -> (a != 0 || b != 0).toInt()
            // never happens, because // is for comments
            "//" -> a.toInt() / b.toInt()
            else -> evaluateDuplicatedOperators(a, b, this)
        }
    }

    private fun evaluateDuplicatedOperators(first: Number, second: Number, token: Token): Number {
        if (first is Double) {
            val a = first.toDouble()
            val b = second.toDouble()
            return when (token.symbol) {
                "-" -> a - b
                "*" -> a * b
                "/" -> a / b
                "%" -> a % b
                else -> throw PositionalException("Operator `${token.symbol}` not implemented", token)
            }
        } else {
            val a = first.toInt()
            val b = second.toInt()
            return when (token.symbol) {
                "-" -> a - b
                "*" -> a * b
                "/" -> a / b
                "%" -> a % b
                else -> throw PositionalException("Operator `${token.symbol}` not implemented", token)
            }
        }
    }

    private fun unifyNumbers(first: Any, second: Any): List<Number> {
        if (first !is Number)
            throw PositionalException("left operand is not numeric for this infix operator", this)
        if (second !is Number)
            throw PositionalException("right operand is not numeric for this infix operator", this)
        if (first is Int && second is Int)
            return listOf(first, second)
        return listOf(first.toDouble(), second.toDouble())
    }

    private fun evaluateUnaryMinus(number: Number): Number = if (number is Double) -number else -(number as Int)

    private fun evaluateNot(symbolTable: SymbolTable): Int {
        val res = left.evaluate(symbolTable)
        if (res is Number)
            return (res == 0).toInt()
        throw PositionalException("! operator applicable to numeric", this)
    }
}
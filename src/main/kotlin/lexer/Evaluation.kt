package lexer

object Evaluation {
    val globalVariables = mutableMapOf<String, Map<String, Any>>()
}

object FunctionEvaluation {
    fun evaluateFunction(token: Token, args: Map<String, Any>) {
        for (stmt in token.children) {
            when (stmt.value) {
                "while" -> ""
                "if" -> ""
                "=" -> ""
                "[" -> ""
                "(" -> ""
                else -> ""
            }
        }
    }
}

object ValueEvaluation {
    fun evaluateValue(token: Token): Any {
        return when (token.symbol) {
            "(NUMBER)" -> if (token.value.contains(".")) token.value.toDouble() else token.value.toInt()
            "(STRING)" -> token.value
            "true" -> 1
            "false" -> 0
            "!" -> if (evaluateValue(token.children[0]) == 0) 1 else 0
            "ARRAY" -> token.children.map { evaluateValue(it) }
            else -> evaluateInfixArithmetic(token)
        }
    }

    private fun evaluateInfixArithmetic(token: Token): Number {
        if (token.children.size == 1) {
            val a = evaluateValue(token.children[0])
            return when (token.symbol) {
                "-" -> evaluateUnaryMinus(a as Number)
                else -> throw PositionalException("no such prefix operator", token.position)
            }
        }
        if (token.children.size == 2) {
            val (a, b) = unifyNumbers(evaluateValue(token.children[0]), evaluateValue(token.children[1]), token)
            return when (token.symbol) {
                ">" -> (a as Double > b as Double).toInt()
                "<" -> (a.toDouble() < b as Double).toInt()
                ">=" -> (a as Double >= b as Double).toInt()
                "<=" -> (a as Double <= b as Double).toInt()
                "==" -> (a == b).toInt()
                "!=" -> (a != b).toInt()
                "&" -> (a != 0 && b != 0).toInt()
                "|" -> (a != 0 || b != 0).toInt()
                "//" -> a.toInt() / b.toInt()
                else -> evaluateDuplicatedOperators(a, b, token)
            }
        } else throw PositionalException("expected infix operator", token.position)
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
                else -> throw PositionalException("operator ${token.symbol} not implemented", token.position)
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
                else -> throw PositionalException("operator ${token.symbol} not implemented", token.position)
            }
        }
    }

    private fun unifyNumbers(first: Any, second: Any, token: Token): List<Number> {
        if (first !is Number)
            throw PositionalException("left operand is not numeric for this infix operator", token.position)
        if (second !is Number)
            throw PositionalException("right operand is not numeric for this infix operator", token.position)
        if (first is Int && second is Int)
            return listOf(first, second)
        return listOf(first as Double, second as Double)
    }

    private fun evaluateUnaryMinus(number: Number): Number = if (number is Double) -number else -(number as Int)

    private fun Boolean.toInt(): Number = if (this) 1 else 0
}


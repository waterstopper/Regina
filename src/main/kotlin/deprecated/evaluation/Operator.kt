package deprecated.evaluation

import evaluation.ValueEvaluation.toInt

@Deprecated("")
enum class Operator(val precedence: Int) {
    LEFT_PAR(10), // (
    RIGHT_PAR(10), // )

    MUL(1), // *
    DIV(1), // /
    INT_DIV(1), // //
    ADD(2), // +
    SUB(2), // -

    MORE(3), // >
    LESS(3), // <
    MORE_EQUAL(3), // >=
    LESS_EQUAL(3), // <=
    EQUAL(4), // ==
    UNEQUAL(4), // !=

    LAND(5), // &
    LOR(6), // |

    COND(7), // ?
    DIVIDER(7), // :

    NOT_OPERATOR(-1);

    companion object {
        /**
         * * Letters, digits, dots "." and underscores "_" are not allowed in operators.
         * * Operators must be either 2 chars or 1 char
         */
        fun toOperator(s: String): Operator {
            return when (s) {
                "//" -> INT_DIV
                ">=" -> MORE_EQUAL
                "<=" -> LESS_EQUAL
                "==" -> EQUAL
                "!=" -> UNEQUAL
                "&&" -> LAND
                "||" -> LOR
                else -> NOT_OPERATOR
            }
        }

        fun toOperator(c: Char): Operator {
            return when (c) {
                '(' -> LEFT_PAR
                ')' -> RIGHT_PAR

                '*' -> MUL
                '/' -> DIV
                '\\' -> INT_DIV
                '+' -> ADD
                '-' -> SUB

                '>' -> MORE
                '<' -> LESS
                ']' -> MORE_EQUAL
                '[' -> LESS_EQUAL
                '=' -> EQUAL
                '!' -> UNEQUAL

                '&' -> LAND
                '|' -> LOR

                '?' -> COND
                ':' -> DIVIDER
                else -> NOT_OPERATOR
            }
        }

        fun calc(operator: Operator, first: Number, second: Number): Number {
            val a = if (second is Int) second.toDouble() else second as Double
            val b = if (first is Int) first.toDouble() else first as Double
            val res = when (operator) {
                MUL -> a * b
                DIV -> a / b
                INT_DIV -> (second.toInt()) / (first.toInt())

                ADD -> a + b
                SUB -> a - b

                MORE -> (a > b).toInt()
                LESS -> (a < b).toInt()
                MORE_EQUAL -> (a >= b).toInt()
                LESS_EQUAL -> (a <= b).toInt()
                EQUAL -> (a == b).toInt()
                UNEQUAL -> (a != b).toInt()

                LAND -> (a != 0.0 && b != 0.0).toInt()
                LOR -> (a != 0.0 || b != 0.0).toInt()
                else -> throw Exception("invalid operator \"$operator\"")
            }
            return if (first is Int && second is Int
                || operator == INT_DIV
                || operator.ordinal > 6
            ) res.toInt() else res
        }
    }
}
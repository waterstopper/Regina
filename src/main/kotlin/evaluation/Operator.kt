package evaluation

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
                Operator.MUL -> a * b
                Operator.DIV -> a / b
                Operator.INT_DIV -> (second.toInt()) / (first.toInt())

                Operator.ADD -> a + b
                Operator.SUB -> a - b

                Operator.MORE -> if (a > b) 1 else 0
                Operator.LESS -> if (a < b) 1 else 0
                Operator.MORE_EQUAL -> if (a >= b) 1 else 0
                Operator.LESS_EQUAL -> if (a <= b) 1 else 0
                Operator.EQUAL -> if (a == b) 1 else 0
                Operator.UNEQUAL -> if (a != b) 1 else 0

                Operator.LAND -> if (a != 0.0 && b != 0.0) 1 else 0
                Operator.LOR -> if (a != 0.0 || b != 0.0) 1 else 0
                else -> throw Exception("invalid operator \"$operator\"")
            }
            return if (first is Int && second is Int
                || operator == Operator.INT_DIV
                || operator.ordinal > 6
            ) res.toInt() else res
        }
    }
}
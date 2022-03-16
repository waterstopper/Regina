package evaluation

import java.util.*

object Algebra {
    /**
     * evaluate all that is not ternary
     */
    fun evaluate(input: String): Number {
        // already transformed
        val expr = input//transform(input)
        val operators = Stack<Operator>()
        val values = Stack<Number>()
        var i = 0

        while (i <= expr.lastIndex) {
            when {
                expr[i].isDigit() -> i = evalNumber(i, expr, values)
                //expr[i] == '(' -> operators.push(Operator.LEFT_PAR)
                //expr[i] == ')' -> evalRightParentheses(operators, values)
                expr[i] == '-' -> i = evalMinus(i, expr, values, operators)
                else -> {
                    if (Operator.toOperator(expr[i].toString() + expr[i + 1].toString()) != Operator.NOT_OPERATOR) {
                        evalOperator(
                            Operator.toOperator(expr[i].toString() + expr[i + 1].toString()),
                            operators,
                            values
                        )
                        i++
                    } else evalOperator(Operator.toOperator(expr[i]), operators, values)
                }
            }
            i++
        }

        // calc all what is left
        while (operators.isNotEmpty())
            values.push(Operator.calc(operators.pop(), values.pop(), values.pop()))

        // after all calculations values contains one element which is the result
        return values.pop()
    }
    //endregion

    //region Operators and numbers evaluation
    private fun evalNumber(index: Int, expr: String, values: Stack<Number>): Int {
        var i = index
        var isDouble = false
        var number = expr[i].toString()
        while (i < expr.lastIndex && (expr[i + 1].isDigit() || expr[i + 1] == '.')) {
            if (expr[i + 1] == '.')
                isDouble = true
            i++
            number += expr[i].toString()
        }
        values.push(if (isDouble) number.toDouble() else number.toInt())

        return i
    }

    /**
     * Checking if minus is unary or not
     * and acting based on it
     */
    private fun evalMinus(i: Int, expr: String, values: Stack<Number>, operators: Stack<Operator>): Int {
        var minusCount = 1
        var j = i + 1
        while (j <= expr.lastIndex) {
            if (expr[j] == '-')
                minusCount++
            else break
            j++
        }
        j--
        // binary
        if (values.size == operators.count { it != Operator.LEFT_PAR && it != Operator.RIGHT_PAR && it != Operator.NOT_OPERATOR } + 1) {
            evalOperator(if (minusCount % 2 == 0) Operator.ADD else Operator.SUB, operators, values)
            return j
        }
        //unary
        return evalNumber(j, expr, values)
    }

    private fun evalOperator(op: Operator, operators: Stack<Operator>, values: Stack<Number>) {
        // should not calc
        if (operators.isNotEmpty() && operators.peek().precedence > op.precedence)
            operators.push(op)
        else {
            // should calc previous operator
            if (operators.isNotEmpty())
                values.push(Operator.calc(operators.pop(), values.pop(), values.pop()))
            operators.push(op)
        }
    }
    //endregion
}
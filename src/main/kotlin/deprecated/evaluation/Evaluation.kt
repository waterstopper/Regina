package deprecated.evaluation

import java.util.*
@Deprecated("")
object Evaluation {
    //region Expression deprecated.connections.evaluation
    // region Evaluation steps
    fun evaluate(input: String): Any {
        var expr = transform(input)

        expr = evaluateFunctions(expr)
        // evaluate all parentheses
        expr = evaluateParentheses(expr)
        // evaluate all ternary
        expr = evaluateTernary(expr)
        if (expr.startsWith("--")) {
            println()
        }
        return evaluateWordsOrAlgebra(expr)
    }

    fun evaluateWordsOrAlgebra(expr: String): Any {
        if (evaluateWords(expr))
            return expr

        return Algebra.evaluate(expr)
    }

    private fun evaluateWords(expr: String): Boolean = expr.matches(Regex("[a-zA-Z]\\w+", RegexOption.CANON_EQ))

    /**
     * here I suppose that braces cannot be nested.
     * If they are nested, change the algorithm to parentheses deprecated.connections.evaluation
     */
    private fun evaluateFunctions(input: String): String {
        var expr = input

        while (expr.contains('{')) {
            val start = expr.indexOf('{') + 1
            var end = start + 1
            while (expr[end] != '}')
                end++

            val res = Function.evalFunction(expr.substring(start until end))
            expr = expr.replaceRange(start - 1..end, res.toString())
        }

        return expr
    }

    private fun evaluateParentheses(input: String): String {
        var expr = input
        while (expr.contains('(')) {
            var sum = 1
            val start = expr.indexOf('(')
            var end = start + 1

            while (sum != 0) {
                if (expr[end] == '(')
                    sum++
                else if (expr[end] == ')')
                    sum--
                end++
            }
            end--

            // essentially change everything that's in the parentheses to single number
            expr = expr.replaceRange(start..end, evaluate(expr.substring(start + 1, end)).toString())
        }

        return expr
    }

    private fun evaluateTernary(input: String): String {
        var expr = input
        // handling all ternary operators.
        // deprecated.connections.evaluation from right to left!
        while (expr.contains('?')) {
            var i = expr.lastIndex
            var lastIndex = expr.lastIndex
            var semicolonIndex = lastIndex + 1

            while (expr[i] != '?') {
                if (expr[i] == ':') {
                    lastIndex = semicolonIndex - 1
                    semicolonIndex = i
                }
                i--
            }
            val questionIndex = i
            i--
            while (i >= 0 && expr[i] != '?' && expr[i] != ':') {
                i--
            }
            // true
            expr = if (Algebra.evaluate(expr.substring(i + 1, questionIndex)) != 0)
                expr.replaceRange(
                    i + 1..lastIndex,
                    evaluateWordsOrAlgebra(expr.substring(questionIndex + 1, semicolonIndex)).toString()
                )
            else
                expr.replaceRange(
                    i + 1..lastIndex,
                    evaluateWordsOrAlgebra(expr.substring(semicolonIndex + 1, lastIndex + 1)).toString()
                )
        }

        return expr
    }

    private fun evalRightParenthesis(operators: Stack<Operator>, values: Stack<Number>) {
        var current = operators.pop()
        while (current != Operator.LEFT_PAR) {
            values.push(Operator.calc(current, values.pop(), values.pop()))
            current = operators.pop()
        }
    }


    /**
     * remove all whitespaces and
     * transform all operators to one-symbol chars
     */
    private fun transform(expr: String): String {
        return expr
            .replace("\\s".toRegex(), "").replace("@", "")
    }


    fun Char.isNameSymbol(): Boolean {
        return this.isLetterOrDigit() || this == '_'
    }
}
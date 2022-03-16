package lexer

import kotlin.reflect.jvm.internal.impl.incremental.components.Position

class OldLexer : Iterator<Token> {
    val NUMBER_NULLS_TOLERATED = 1
    val tokens = mutableListOf<Token>()
    var iter = 0
    val position: Pair<Int, Int> = Pair(0, 0)

    fun tokenize(input: String) {
        val arr =
            (removeWhitespace(input) + StringBuilder(" ".repeat(NUMBER_NULLS_TOLERATED + 1)).toString()).toCharArray()
        var i = 0
        var lastSuccesfulIndex = 0
        val str = StringBuilder()
        while (i < arr.size) {
            str.append(arr[i])
            if (Symbol.getToken(str.toString()) == null) {
                if (i - lastSuccesfulIndex > NUMBER_NULLS_TOLERATED) {
                    str.delete(str.length - (i - lastSuccesfulIndex), str.length)
                    //tokens.add(Token(Symbol.getToken(str.toString())!!, str.toString(), i))
                    str.clear()
                    if (i == arr.lastIndex)
                        break
                    i = lastSuccesfulIndex + 1
                } else
                    i++
            } else {
                lastSuccesfulIndex = i
                i++
            }
        }
    }

    private fun removeWhitespace(str: String): String = str.replace(Regex("\\s"), "")

    override fun next(): Token = tokens[iter++]

    override fun hasNext(): Boolean = iter < tokens.size

    fun peek(): Token = tokens[iter]
}
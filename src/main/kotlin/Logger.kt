import token.Token

object Logger {
    val warnings = mutableListOf<Pair<Pair<Int, Int>, String>>()
    var error = Pair(Pair(-1, -1), "")

    fun addWarning(token: Token, message: String) = warnings.add(Pair(token.position, message))

    fun send(): List<Pair<Pair<Int, Int>, String>> {
        return if (error.first == Pair(-1, -1))
            warnings
        else warnings + error
    }
}
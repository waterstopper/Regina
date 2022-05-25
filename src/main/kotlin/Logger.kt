import token.Token

object Logger {
    private val warnings = mutableListOf<Triple<Int, Int, String>>()
    var error: Triple<Int, Int, String>? = null

    fun addWarning(token: Token, message: String) =
        warnings.add(Triple(token.position.first, token.position.second, message))

    fun send(): List<Triple<Int, Int, String>> {
        return if (error == null)
            warnings
        else warnings + error!!
    }
}
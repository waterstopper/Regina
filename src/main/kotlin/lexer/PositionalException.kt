package lexer

class PositionalException(private val errorMessage: String, private val position: Pair<Int, Int>) : Exception() {
    override val message: String
        get() =  "$errorMessage at ${position.second},${position.first}"

}
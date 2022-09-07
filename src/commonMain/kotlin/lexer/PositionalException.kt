package lexer

import node.Node
import properties.Variable
import token.Token
import utils.Utils.mapToString
import kotlin.reflect.KClass

open class PositionalException(
    protected val errorMessage: String,
    private val fileName: String,
    protected val node: Node = Node(),
    protected val position: Pair<Int, Int> = Pair(0, 0),
    protected val length: Int = 1,
) : Exception() {
    override val message: String
        get() = "`${node.value}` $errorMessage at ${getPosition()}"

    protected fun getPosition(): String {
        return if (node.value != "")
            "${node.position.second},${node.position.first}-${node.position.first + node.value.length - 1}"
        else "${position.second},${position.first}-${position.first + length - 1}"
    }
}

open class SyntaxException(
    private val errorMessage: String,
    val fileName: String,
    val token: Token,
    val position: Pair<Int, Int> = Pair(0, 0)
) :
    Exception() {
    override val message: String
        get() = "`${token.value}` $errorMessage at ${getPosition()}"

    private fun getPosition(): String {
        return if (token.value != "")
            "${token.position.second},${token.position.first}-${token.position.first + token.value.length - 1}"
        else "${position.second},${position.first}-${position.first}"
    }
}

class NotFoundException(
    node: Node = Node(),
    fileName: String = "",
    val variable: Variable? = null
) :
    PositionalException("", fileName, node) {
    override val message: String
        get() =
            "Not found " + (variable?.toString() ?: node.value) + " at ${getPosition()}"
}

class TokenExpectedTypeException(
    private val classes: List<KClass<*>>,
    fileName: String,
    token: Token,
    private val value: Any? = null,
    private val expectedMultiple: Boolean = false
) : SyntaxException("", fileName, token) {
    override val message: String
        get() {
            return "Expected " + classes.joinToString(
                separator = if (expectedMultiple) " and " else " or "
            ) {
                mapToString(it)
            } + (if (value != null) ", but got ${
                mapToString(
                    value::class
                )
            }" else "") + " ${token.position}"
        }
}


class ExpectedTypeException(
    private val classes: List<KClass<*>>,
    fileName: String,
    node: Node,
    private val value: Any? = null,
    private val expectedMultiple: Boolean = false
) : PositionalException("", fileName, node) {
    override val message: String
        get() {
            return "Expected " + classes.joinToString(
                separator = if (expectedMultiple) " and " else " or "
            ) {
                mapToString(it)
            } + (if (value != null) ", but got ${
                mapToString(
                    value::class
                )
            }" else "") + " ${node.position}"
        }
}


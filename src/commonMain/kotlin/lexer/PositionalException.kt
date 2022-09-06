package lexer

import delete.Delete
import node.Identifier
import node.Node
import node.invocation.Invocation
import node.operator.Index
import properties.RFunction
import properties.Variable
import properties.primitive.*
import table.FileTable
import token.Token
import utils.Utils.mapToString
import kotlin.reflect.KClass

open class PositionalException(
    protected val errorMessage: String,
    protected val node: Node = Node(),
    protected val position: Pair<Int, Int> = Pair(0, 0),
    protected val length: Int = 1,
    private val file: String = ""
) : Exception() {
    init {
        // TODO why clear table? because it can interrupt evaluation. Cannot do it because try catch in FileTable.getFunctionOrNull
        //  clear()
    }

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

class RuntimeError(errorMessage: String, private val delete: Delete) : PositionalException(errorMessage) {
    override val message: String
        get() = "$errorMessage at ${delete.position}"
}

class NotFoundException(
    node: Node = Node(),
    fileName: String = "",
    file: FileTable = FileTable("", -1),
    val variable: Variable? = null
) :
    PositionalException("", node, file = if (fileName == "") file.fileName else fileName) {
    override val message: String
        get() =
            "Not found " + (variable?.toString() ?: node.value) + " at ${getPosition()}"
}

class TokenExpectedTypeException(
    private val classes: List<KClass<*>>,
    token: Token,
    private val value: Any? = null,
    private val expectedMultiple: Boolean = false
) : SyntaxException("", token) {
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
    node: Node,
    private val value: Any? = null,
    private val expectedMultiple: Boolean = false
) : PositionalException("", node) {
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

    private fun mapToString(mapped: KClass<*>): String {
        return when (mapped) {
            RFunction::class -> "Function"
            PInt::class -> "Int"
            PDouble::class -> "Double"
            PNumber::class -> "Number"
            PString::class -> "String"
            PArray::class -> "Array"
            PDictionary::class -> "Dictionary"
            Identifier::class -> "Identifier"
            Invocation::class -> "Invocation"
            Index::class -> "Index"
            else -> mapped.toString().split(".").last()
        }
    }
}

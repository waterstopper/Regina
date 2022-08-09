package lexer

import evaluation.Evaluation.clear
import delete.Delete
import properties.Function
import properties.Variable
import properties.primitive.*
import table.FileTable
import node.Identifier
import node.Node
import node.invocation.Invocation
import node.operator.Index
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
        clear()
    }

    override val message: String
        get() = "`${node.value}` $errorMessage at ${getPosition()}"

    protected fun getPosition(): String {
        return if (node.value != "")
            "${node.position.second},${node.position.first}-${node.position.first + node.value.length - 1}"
        else "${position.second},${position.first}-${position.first + length - 1}"
    }
}

class RuntimeError(errorMessage: String, private val delete: Delete) : PositionalException(errorMessage) {
    override val message: String
        get() = "$errorMessage at ${delete.position}"
}

class NotFoundException(
    node: Node = Node(),
    fileName: String = "",
    file: FileTable = FileTable(""),
    val variable: Variable? = null
) :
    PositionalException("", node, file = if (fileName == "") file.fileName else fileName) {
    override val message: String
        get() =
            "Not found " + (variable?.toString() ?: node.value) + " at ${getPosition()}"
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
            Function::class -> "Function"
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

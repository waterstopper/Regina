package lexer

import properties.Function
import properties.Variable
import properties.primitive.*
import table.FileTable
import token.Identifier
import token.Token
import token.invocation.Invocation
import token.operator.Index
import kotlin.reflect.KClass

open class PositionalException(
    private val errorMessage: String,
    private val token: Token = Token(),
    private val position: Pair<Int, Int> = Pair(0, 0),
    private val length: Int = 1,
    private val file: String = ""
) : Exception() {
    override val message: String
        get() = if (token.value != "")
            "`${token.value}` $errorMessage at ${token.position.second},${token.position.first}-${token.position.first + token.value.length - 1}"
        else "$errorMessage at ${position.second},${position.first}-${position.first + length - 1}"
}

class NotFoundException(
    token: Token = Token(),
    fileName: String = "",
    file: FileTable = FileTable(""),
    val variable: Variable? = null
) :
    PositionalException("", token, file = if (fileName == "") file.fileName else fileName) {
    override val message: String
        get() = "Not found" + (variable?.toString() ?: "")
}

class ExpectedTypeException(
    private val classes: List<KClass<*>>,
    token: Token,
    private val value: Any? = null,
    private val expectedMultiple: Boolean = false
) : PositionalException("", token) {
    override val message: String
        get() {
            return "Expected " + classes.joinToString(
                separator = if (expectedMultiple) " and " else " or "
            ) { mapToString(it) } + if (value != null) ", but got ${mapToString(value::class)}" else ""
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
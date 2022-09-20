import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

expect object FileSystem {
    fun read(path: String): String
    fun write(path: String, content: String)
    fun exists(path: String): Boolean
    fun delete(path: String): Boolean
}

expect fun readLine(): String
expect fun round(num: Double, digits: Int): Double

expect fun preload(fileNames: List<String>)

expect fun isInt(num: Any): Boolean

expect fun isDouble(num: Any): Boolean

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Message(val type: String, val content: Any)

expect fun sendMessage(m: Message)


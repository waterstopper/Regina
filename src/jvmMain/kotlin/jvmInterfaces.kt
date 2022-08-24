import java.io.File
import kotlin.math.floor

actual object FileSystem {

    actual fun read(path: String): String {
        return File(path).readText()
    }

    actual fun write(path: String, content: String) {
        File(path).writeText(content)
    }

    actual fun exists(path: String): Boolean {
        return File(path).exists()
    }

    actual fun delete(path: String): Boolean {
        return File(path).delete()
    }
}

actual fun readLine(): String {
    return kotlin.io.readLine() ?: ""
}

actual fun round(num: Double, digits: Int): Double {
    return String.format("%.${digits}f", num).replace(',', '.').toDouble()
}

actual fun preload(fileNames: List<String>) {
}

actual fun isInt(num: Any): Boolean = num is Number && num.toDouble() == floor(num.toDouble())
actual fun isDouble(num: Any): Boolean = num is Number && num.toDouble() != floor(num.toDouble())
actual fun sendMessage(m: Message) {
    println(m)
}
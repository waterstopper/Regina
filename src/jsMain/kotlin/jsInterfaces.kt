import kotlinx.browser.localStorage
import lexer.PositionalException

actual object FileSystem {
    var fileSystem = mutableMapOf<String, String>()

    actual fun read(path: String): String {
        return fileSystem[path] ?: throw PositionalException("File not found $path")
//        if (JSSetter.readText == null) {
//            val res = window.fetch("https://alex5041.github.io/").await().text().await()
//            println(res)
//            return res//localStorage.getItem(path) ?: throw Exception("`$path` not found in file system")
//        } else {
//            return JSSetter.readText!!(path)
//        }
    }

    actual fun write(path: String, content: String) {
        sendMessage(Message("write", Pair(path, content)))
        fileSystem[path] = content
//        if (JSSetter.writeText == null)
//            localStorage.setItem(path, content)
//        else JSSetter.writeText!!(path, content)
    }

    fun addFile(name: String, content: String) {
        localStorage.setItem(name, content)
    }

    actual fun exists(path: String): Boolean {
        return fileSystem[path] != null
    }

    actual fun delete(path: String): Boolean {
        fileSystem.remove(path)
        return true
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
object JSSetter {
    var readLine: (() -> String)? = null
    var readText: ((String) -> String)? = null
    var writeText: ((String, String) -> String)? = null
    var sendMessage: ((Message) -> Unit)? = null
}

actual fun readLine(): String = ""

// https://stackoverflow.com/a/48764436
actual fun round(num: Double, digits: Int): Double = js(
    """
    var nnum = num < 0 ? -num : num;
    var p = Math.pow(10, digits);
    var n = (nnum * p).toPrecision(15);
    return num < 0 ? -Math.round(n) / p : Math.round(n) / p;
"""
) as Double

suspend fun jsEvaluate() {

}

actual fun preload(fileNames: List<String>) {
    for (name in fileNames) {
        when (name) {
            "std/geometry2D.rgn" -> addGeometry2D()
            "std/math.rgn" -> addMath()
            "std/svg.rgn" -> addSvg()
            "src/commonTest/resources/std/geometry2DTest.rgn" -> addGeometryTest()
            "src/commonTest/resources/std/mathTest.rgn" -> addMathTest()
            "src/commonTest/resources/testCode.rgn" -> addGenericTest()
            else -> throw Exception("File not found $name")
        }
    }
}

actual fun isInt(num: Any): Boolean {
    return js("Number.isInteger(num)") as Boolean
}

actual fun isDouble(num: Any): Boolean {
    return js(
        """
        typeof num === 'number' &&
    !Number.isNaN(num) &&
    !Number.isInteger(num)
    """
    ) as Boolean
}

actual fun sendMessage(m: Message) {
    println(m)
    JSSetter.sendMessage?.let { it(m) }
}
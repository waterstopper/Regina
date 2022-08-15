expect object FileSystem {
    fun read(path: String): String
    fun write(path: String, content: String)
    fun exists(path: String): Boolean
    fun delete(path: String): Boolean
}

expect fun readLine(): String
expect fun round(num:Double, digits: Int): Double
//expect fun String.format()

expect fun runTest(block: suspend () -> Unit)

expect fun preload(fileNames:List<String>)

expect fun isInt(num:Any):Boolean

expect fun isDouble(num: Any):Boolean
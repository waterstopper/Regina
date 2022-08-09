import node.Node

object Logger {
    private val warnings = mutableListOf<Triple<Int, Int, String>>()
    var error: Triple<Int, Int, String>? = null

    fun addWarning(node: Node, message: String) =
        warnings.add(Triple(node.position.first, node.position.second, message))

    fun send(): List<Triple<Int, Int, String>> {
        return if (error == null)
            warnings
        else warnings + error!!
    }
}

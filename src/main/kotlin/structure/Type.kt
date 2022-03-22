package structure

class Type(
    name: String,
    val typeName:String,
    val type: Type?,
    parent: Type?,
    val assignments: MutableList<Assignment>,
    val exported: Any? = null
) :
    Node(name, parent) {
    val properties: MutableList<Node> = mutableListOf()
    val functions: MutableList<Function> = mutableListOf()

    override fun toString(): String {
        return "$name${if (type != null) " : $type" else ""}${if (exported != null) " -> $exported" else ""}"
    }
}

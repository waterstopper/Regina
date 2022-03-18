package structure

class Type(
    name: String,
    val type: Type?,
    parent: Type?,
    val assignments: MutableList<Assignment>,
    val exported: Any? = null
) :
    Node(name, parent) {
    val resolved: MutableMap<String, Node> = mutableMapOf()

}

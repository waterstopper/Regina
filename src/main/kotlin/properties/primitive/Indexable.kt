package properties.primitive

import node.Node
import properties.Variable

interface Indexable {
    operator fun get(index: Any, node: Node): Any
    operator fun set(index: Any, value: Any, nodeIndex: Node, nodeValue: Node)
    fun checkIndexType(index: Variable): Boolean
}

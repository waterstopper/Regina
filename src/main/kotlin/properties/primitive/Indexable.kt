package properties.primitive

import properties.Variable
import node.Node

interface Indexable {
    operator fun get(index: Any, node: Node): Any
    operator fun set(index: Any, value: Any, nodeIndex: Node, nodeValue: Node)
    fun checkIndexType(index: Variable) : Boolean
}

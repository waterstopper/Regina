package properties.primitive

import node.Node
import properties.Variable
import table.FileTable

interface Indexable {
    operator fun get(index: Any, node: Node, fileTable: FileTable): Any
    operator fun set(index: Any, value: Any, nodeIndex: Node, nodeValue: Node, fileTable: FileTable)
    fun checkIndexType(index: Variable): Boolean
}

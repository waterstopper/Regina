package properties

import References
import node.Node
import properties.primitive.PDictionary
import table.FileTable

abstract class Variable {
    abstract fun getPropertyOrNull(name: String): Property?
    abstract fun getProperty(node: Node, fileTable: FileTable): Property
    abstract fun getFunctionOrNull(node: Node): RFunction?
    abstract fun getFunction(node: Node, fileTable: FileTable): RFunction
    abstract fun getProperties(): PDictionary
    abstract fun toDebugClass(references: References, copying: Boolean = false): Pair<String, Any>
    abstract fun copy(deep: Boolean = true): Variable
}

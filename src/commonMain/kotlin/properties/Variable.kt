package properties

import Debug
import References
import node.Node
import properties.primitive.PDictionary
import table.FileTable

abstract class Variable(var parent: Type?) {
    abstract fun getPropertyOrNull(name: String): Property?
    abstract fun getProperty(node: Node): Property
    abstract fun getFunctionOrNull(node: Node): RFunction?
    abstract fun getFunction(node: Node, fileTable: FileTable): RFunction
    abstract fun getProperties(): PDictionary
    abstract fun toDebugClass(references: References): Any
}

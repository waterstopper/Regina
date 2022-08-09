package properties

import properties.primitive.PDictionary
import node.Node

abstract class Variable(var parent: Type?) {
    abstract fun getPropertyOrNull(name: String): Property?
    abstract fun getProperty(node: Node): Property
    abstract fun getFunctionOrNull(node: Node): Function?
    abstract fun getFunction(node: Node): Function
    abstract fun getProperties(): PDictionary
}

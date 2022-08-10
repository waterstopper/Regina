package properties

import node.Node
import properties.primitive.PDictionary

abstract class Variable(var parent: Type?) {
    abstract fun getPropertyOrNull(name: String): Property?
    abstract fun getProperty(node: Node): Property
    abstract fun getFunctionOrNull(node: Node): Function?
    abstract fun getFunction(node: Node): Function
    abstract fun getProperties(): PDictionary
}

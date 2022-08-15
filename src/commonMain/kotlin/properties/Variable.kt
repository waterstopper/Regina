package properties

import node.Node
import properties.primitive.PDictionary

abstract class Variable(var parent: Type?) {
    abstract fun getPropertyOrNull(name: String): Property?
    abstract fun getProperty(node: Node): Property
    abstract fun getFunctionOrNull(node: Node): RFunction?
    abstract fun getFunction(node: Node): RFunction
    abstract fun getProperties(): PDictionary
}

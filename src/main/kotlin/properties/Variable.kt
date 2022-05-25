package properties

import properties.primitive.PDictionary
import token.Token

abstract class Variable(var parent: Type?) {
    abstract fun getPropertyOrNull(name: String): Property?
    abstract fun getProperty(token: Token): Property
    abstract fun getFunctionOrNull(name: String): Function?
    abstract fun getFunction(token: Token): Function
    abstract fun hasProperty(token: Token): Boolean
    abstract fun getProperties(): PDictionary
}
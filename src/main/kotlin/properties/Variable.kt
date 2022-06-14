package properties

import properties.primitive.PDictionary
import token.Token

abstract class Variable(var parent: Type?) {
    abstract fun getPropertyOrNull(name: String): Property?
    abstract fun getProperty(token: Token): Property
    abstract fun getFunctionOrNull(token: Token): Function?
    abstract fun getFunction(token: Token): Function
    abstract fun getProperties(): PDictionary
}
package properties

import token.Token

abstract class Variable(var parent: Type?) {
    abstract fun getPropertyOrNull(name: String): Property?
    abstract fun getProperty(token: Token): Property
    abstract fun getFunctionOrNull(name: String): Function?
    abstract fun getFunction(token: Token): Function
    abstract fun hasProperty(token: Token): Boolean
}
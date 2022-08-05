package properties.primitive

import properties.Variable
import token.Token
import kotlin.reflect.KClass

interface Indexable {
    operator fun get(index: Any, token: Token): Any
    operator fun set(index: Any, value: Any, tokenIndex: Token, tokenValue: Token)
    fun checkIndexType(index: Variable) : Boolean
}

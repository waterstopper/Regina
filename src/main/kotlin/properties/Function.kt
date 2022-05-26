package properties

import token.Token
import token.statement.Assignment

open class Function(
    val name: String,
    val params: List<Token>,
    val withDefaultParams: List<Assignment>,
    val body: Token
) :
    Invokable {
    override fun toString(): String = "$name(${params.joinToString(separator = ",")})"

    // TODO wtf is this equals
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Function) return false
        if (params.size != other.params.size) return false
        return true
    }

    fun hasParam(name: String): Boolean = params.any { it.value == name } || withDefaultParams.any { it.name == name }


    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + params.size.hashCode()
        return result
    }
}
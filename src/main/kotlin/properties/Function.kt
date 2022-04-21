package properties

import token.Token

open class Function(val name: String, val params: List<String>, val body: Token) :
    Invokable {
    override fun toString(): String {
        return "$name(${params.joinToString(separator = ",")})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Function) return false
        if (params.size != other.params.size) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + params.size.hashCode()
        return result
    }
}
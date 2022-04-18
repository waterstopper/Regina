package properties

import SymbolTable
import SymbolTable.Type
import token.Token

open class Function(name: String, val params: List<String>, val body: Token, parent: Type? = null) :
    Property(name, parent), Invokable {
    override val symbolTable: SymbolTable = SymbolTable()
    override fun toString(): String {
        return "$name(${params.joinToString(separator = ",")})     ${parent ?: ""}"
    }

    fun getFunctionName() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Function) return false
        if (!super.equals(other)) return false

        if (params.size != other.params.size) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + params.size.hashCode()
        return result
    }
}
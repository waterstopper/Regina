package properties

import SymbolTable
import SymbolTable.Type

abstract class Variable(name: String, val parent: Type?) {
    abstract val symbolTable:SymbolTable
    protected var name = name
        set(_) {
            println("BAD")
        }


    override fun toString() = "$name:$parent"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Variable) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
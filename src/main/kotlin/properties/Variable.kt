package properties

abstract class Variable(var name: String, val parent: Type?) {
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
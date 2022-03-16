open class Node(open val name: String, open var parent: Container?) {


    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Node)
            return false
        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}

package deprecated
@Deprecated("")
open class OldNode(open val name: String, open var parent: OldContainer?) {


    override fun equals(other: Any?): Boolean {
        if (other == null || other !is OldNode)
            return false
        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}

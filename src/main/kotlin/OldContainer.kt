/**
 *  pipeline:
 *  1. get all overrides from parent
 *  2. get rest of the properties
 *  (on step 1-2 if we need an object which is not instantiated, we push it in stack)
 *  3. check everything on stack while nothing from stack cannot be assigned
 *
 *  one parent cannot have two children with same name!
 */
open class OldContainer(
    name: String, parent: OldContainer?, val declarations: MutableMap<String, Formula>,
) : OldNode(name, parent) {
    val children = mutableListOf<OldNode>()

    fun copy(name: String, parent: OldContainer, type: String): OldContainer {
        val res = OldContainer(name, parent, declarations.toMutableMap())
        res.children.add(OldProperty("type", res, type))
        return res
    }
}

class DefaultOldContainer(name: String, override var parent: OldContainer?, declarations: MutableMap<String, Formula>) :
    OldContainer(name, parent, declarations)
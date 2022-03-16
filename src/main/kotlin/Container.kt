/**
 *  pipeline:
 *  1. get all overrides from parent
 *  2. get rest of the properties
 *  (on step 1-2 if we need an object which is not instantiated, we push it in stack)
 *  3. check everything on stack while nothing from stack cannot be assigned
 *
 *  one parent cannot have two children with same name!
 */
open class Container(
    name: String, parent: Container?, val declarations: MutableMap<String, Formula>,
) : Node(name, parent) {
    val children = mutableListOf<Node>()

    fun copy(name: String, parent: Container, type: String): Container {
        val res = Container(name, parent, declarations.toMutableMap())
        res.children.add(Property("type", res, type))
        return res
    }
}

class DefaultContainer(name: String, override var parent: Container?, declarations: MutableMap<String, Formula>) :
    Container(name, parent, declarations)
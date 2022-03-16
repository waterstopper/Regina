import java.util.*

/**
 * Do BFS for each NodeDeclaration children
 */
class TreeBuilder {
    companion object {
        val definitions = mutableListOf<Container>()

        /**
         * TODO - collect all property values missing in the container
         */
        fun getType(container: Container): String {
//            var res: Container? = container
//            var name = ""
//            do {
//                name = res!!.name
//                res =
//                    definitions.find { it.name == ((container.children.find { c -> c.name == "type" } as Container).children[0] as Property).value.toString() }
//            } while (res != null && res.name != name)

            return "Line"
        }
    }

    val root = definitions.find { it.name == "Root" }!!


    fun resolveTree() {
        do {
            val current = bfs(root) ?: break
            if (current.propertyName == "x2") {
                println()
            }
            processNode(current)
        } while (true)

        println()
    }

    // 1. cannot init Node because it needs something nonexistent yet - add Node to stack and so on.
    // 2. when finally stopped, push properties from the stack initializing each.
    // 3. while doing this might encounter not yet initialized Node again - just go to step 1


    // 1. init Node

    /**
     * Find unresolved declarations
     */
    private fun bfs(root: Container): AssignmentFormula? {
        val stack = Stack<Container>()
        stack.add(root)

        while (stack.isNotEmpty()) {
            val current = stack.pop()
            if (current.declarations.isNotEmpty()) {
                val futureProperty = current.declarations.keys.first()
                return AssignmentFormula(current.declarations[futureProperty]!!.content, futureProperty, current)
            }

            val containers = current.children.filterIsInstance<Container>()
            stack.addAll(containers)
        }

        return null
    }

    private fun processNode(starting: AssignmentFormula) {
        val stack = Stack<AssignmentFormula>()
        stack.push(starting)

        while (stack.isNotEmpty()) {
            val current = stack.peek()
            // evaluate formula into node
            if (current.canEvaluate()) {
                val node = current.evaluate()
                current.parent.children.add(node)
                current.parent.declarations.remove(node.name)
                stack.pop()
            } else
                processChain(current, stack)
        }
    }

    private fun processChain(current: AssignmentFormula, stack: Stack<AssignmentFormula>) {

        var nodeList = current.neededForEvaluation()
        var parent = current.parent
        while (nodeList.isNotEmpty()) {
            if (parent.declarations[nodeList[0]] != null) {
                stack.push(
                    AssignmentFormula(
                        parent.declarations[nodeList[0]]!!.content,
                        nodeList[0],
                        parent
                    )
                )
                break
            } else if (parent.children.any { it.name == nodeList[0] }) {
                val smth = parent.children.find { it.name == nodeList[0] }
                // take value from existing node
                if (smth is Property) {
                    current.replaceFirst(smth.value)
                    // stack.push(current)
                    break
                    // go one node deeper on chain
                } else {
                    parent = smth as Container
                    nodeList = nodeList.drop(1)
                }
            } else if (nodeList[0] == "parent") {
                parent = parent.parent!!
                nodeList = nodeList.drop(1)
            } else throw Exception("no property with name ${nodeList[0]} at $current")
        }
    }
}
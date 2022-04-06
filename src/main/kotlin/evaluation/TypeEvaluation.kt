package evaluation

import evaluation.Evaluation.evaluateInvocation
import evaluation.Evaluation.globalTable
import lexer.PositionalException
import lexer.Token
import properties.Assignment
import properties.Type
import properties.primitive.Primitive
import java.util.*


/**
 * Do BFS for each NodeDeclaration children
 */
object TypeEvaluation {
    fun resolveTree(root: Type):Type {
        do {
            val current = bfs(root) ?: break
            processNode(current)
        } while (true)
        return root
    }

    /**
     * Find unresolved assignments
     */
    private fun bfs(root: Type): Assignment? {
        val stack = Stack<Type>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val current = stack.pop()
            if (current.assignments.isNotEmpty())
                return current.assignments.first()
            val containers = current.symbolTable.variables.values.filterIsInstance<Type>()
            stack.addAll(containers)
        }
        return null
    }

    private fun processNode(starting: Assignment) {
        val stack = Stack<Assignment>()
        stack.push(starting)
        while (stack.isNotEmpty()) {
            val current = stack.peek()
            // evaluate assignment into node
            if (current.canEvaluate()) {
                val node = current.evaluate()
                current.parent.symbolTable.variables[current.name] = node
                current.parent.assignments.remove(current)
                stack.pop()
            } else
                if (current.token.find(".") != null)
                    processLink(current, stack)
                else if (current.token.right.find("(") != null)
                //    evaluateValue(current.token.children[1], current.parent.symbolTable)
                else if (current.token.right.find("(IDENT)") != null)
                    processIdentifier(current, stack)

        }
    }

    private fun processIdentifier(current: Assignment, stack: Stack<Assignment>) {
        val identifier = current.token.right.find("(IDENT)")!!
        val property = current.parent.symbolTable.variables[identifier.value]
        if (property != null) {
            if (property is Primitive) {
                identifier.value = property.value.toString()
                identifier.symbol = property.getSymbol()
            } else throw PositionalException("primitive expected", identifier)
        } else {
            val assignment = current.parent.assignments.find { it.name == identifier.value }
            if (assignment != null)
                stack.add(assignment)
            else throw PositionalException("no such property found", identifier)
        }
    }

    private fun processLink(current: Assignment, stack: Stack<Assignment>) {
        var nextToken = current.getLink()
        var parent = current.parent
        // TODO not considering (if a.b > 0 a.b else a.c).f
        while (nextToken.value == ".") {
            when (nextToken.left.symbol) {
                "parent" -> {
                    parent = parent.parent
                        ?: throw PositionalException("no parent in root class", nextToken.left)
                }
                "(IDENT)" -> {
                    val assignment = current.parent.assignments.find { it.name == nextToken.left.value }
                    if (assignment != null) {
                        stack.add(assignment)
                        return
                    } else if (parent.symbolTable.variables[nextToken.left.value] != null)
                        parent = parent.symbolTable.variables[nextToken.left.value] as Type
                    else if (globalTable.variables[nextToken.value] != null)
                        parent = globalTable.variables[nextToken.value] as Type
                    else throw PositionalException("no such identifier found", nextToken)
                }
                "(" -> {
                    val invocation = evaluateInvocation(nextToken.left, parent.symbolTable)
                    if (invocation is Type)
                        parent = invocation
                    else throw PositionalException("only class can have property", nextToken)
                }
                else -> throw PositionalException("this token is not expected", nextToken.left)
            }
            nextToken = nextToken.right
        }
        when (nextToken.symbol) {
            "parent" -> {
                parent = parent.parent
                    ?: throw PositionalException("no parent in root class", nextToken.left)
            }
            "(IDENT)" -> {
                val assignment = current.parent.assignments.find { it.name == nextToken.value }
                if (assignment != null) {
                    stack.add(assignment)
                    return
                } else if (parent.symbolTable.variables[nextToken.left.value] != null)
                    parent = parent.symbolTable.variables[nextToken.left.value] as Type
                else if (globalTable.variables[nextToken.value] != null)
                    parent = globalTable.variables[nextToken.value] as Type
                else throw PositionalException("no such identifier found", nextToken)
            }
            "(" -> {
            }
            else -> throw PositionalException("this token is not expected", nextToken.left)
        }
        processPrimitiveProperty()
    }

    private fun processPrimitiveProperty() {}

    /**
     * IDENT
     * .
     * ( cannot happen
     */
    private fun processChain(link: Token, current: Assignment, stack: Stack<Assignment>) {
//        var parent = current.parent
//
//        val notResolved = parent.assignments.find { it.name == link.value }
//        if (notResolved != null) {
//            // needed for resolve next
//            stack.push(notResolved)
//            return
//        } else if (parent.symbolTable.variables[link.value] != null) {
//            val smth = parent.symbolTable.variables[link.value]
//            // take value from existing node
//            if (smth is Primitive) {
//                current.replaceFirst(smth.value)
//                // stack.push(current)
//                return
//                // go one node deeper on chain
//            } else {
//                parent = smth as Type
//                linkToken = linkToken.children[1]
//            }
//        } else if (linkToken.children[0].value == "parent") {
//            parent = parent.parent!!
//            linkToken = linkToken.children[1]
//        } else throw PositionalException(
//            "no property with name ${link.value} in ${parent.name}",
//            current.token
//        )
    }
}
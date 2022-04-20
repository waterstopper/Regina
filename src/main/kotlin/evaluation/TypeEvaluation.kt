package evaluation
//
//
//import evaluation.Evaluation.globalTable
//import lexer.PositionalException
//import token.Token
//import evaluation.FunctionEvaluation.toVariable
//import properties.Type
//import properties.primitive.Primitive
//import token.statement.TokenAssignment
//import java.util.*
//
//
///**
// * Do BFS for each NodeDeclaration children
// */
//object TypeEvaluation {
//    var resolving = false
//    fun resolveTree(root: Type): Type {
//        resolving = true
//        do {
//            val current = bfs(root) ?: break
//            processNode(current)
//        } while (true)
//        resolving = false
//        return root
//    }
//
//    /**
//     * Find unresolved assignments
//     */
//    private fun bfs(root: Type): TokenAssignment? {
//        val stack = Stack<Type>()
//        stack.add(root)
//        while (stack.isNotEmpty()) {
//            val current = stack.pop()
//            if (current.assignments.isNotEmpty())
//                return current.assignments.first()
//            val containers = current.symbolTable.getVariableValues().filterIsInstance<Type>()
//            stack.addAll(containers)
//        }
//        return null
//    }
//
//    private fun processNode(starting: TokenAssignment) {
//        val stack = Stack<TokenAssignment>()
//        stack.push(starting)
//        while (stack.isNotEmpty()) {
//            val current = stack.peek()
//            // evaluate assignment into node
//            if (current.canEvaluate()) {
//                val node = current.evaluate(current.parent!!.symbolTable).toVariable(current, current.parent)
//                current.parent!!.symbolTable.addVariable(current.name, node)
//                current.parent!!.assignments.remove(current)
//                stack.pop()
//            } else
//                if (current.find(".") != null) {
//                    val nextInStack = current.parent!!.getFirstUnresolved(current.find(".")!!)
//                    if (nextInStack != null)
//                        stack.push(nextInStack.first.assignments.find { it.name == nextInStack.second }
//                            ?: throw PositionalException(
//                                "no declaration found named ${nextInStack.second}",
//                                current.find(".")!!
//                            ))
//                    processLink(current, stack)
//                } else if (current.right.find("(") != null)
//                //    evaluateValue(current.token.children[1], current.parent.symbolTable)
//                else if (current.right.find("(IDENT)") != null)
//                    processIdentifier(current, stack)
//
//        }
//    }
//
//    private fun processIdentifier(current: TokenAssignment, stack: Stack<TokenAssignment>) {
//        val identifier = current.right.find("(IDENT)")!!
//        val property = current.parent!!.symbolTable.getVariableOrNull(identifier)
//        if (property != null) {
//            if (property is Primitive) {
//                identifier.value = property.value.toString()
//                identifier.symbol = property.getSymbol()
//            } else throw PositionalException("primitive expected", identifier)
//        } else {
//            val assignment = current.parent!!.assignments.find { it.name == identifier.value }
//            if (assignment != null)
//                stack.add(assignment)
//            else throw PositionalException("no such property found", identifier)
//        }
//    }
//
//    private fun processLink(current: TokenAssignment, stack: Stack<TokenAssignment>) {
//        var nextToken = current.find(".")!!
//        var parent = current.parent
//        // TODO not considering (if a.b > 0 a.b else a.c).f
//        while (nextToken.value == ".") {
//            when (nextToken.left.symbol) {
//                "parent" -> {
//                    parent = parent!!.parent
//                        ?: throw PositionalException("no parent in root class", nextToken.left)
//                }
//                "(IDENT)" -> {
//                    val assignment = current.parent!!.assignments.find { it.name == nextToken.left.value }
//                    if (assignment != null) {
//                        stack.add(assignment)
//                        return
//                    } else if (parent!!.symbolTable.getVariableOrNull(nextToken.left) != null)
//                        parent = parent.symbolTable.getVariable(nextToken.left) as Type
//                    else if (globalTable.getVariableOrNull(nextToken) != null)
//                        parent = globalTable.getVariable(nextToken) as Type
//                    else throw PositionalException("no such identifier found", nextToken)
//                }
////                "(" -> {
////                    val invocation = evaluateInvocation(nextToken.left, parent.symbolTable)
////                    if (invocation is Type)
////                        parent = invocation
////                    else throw PositionalException("only class can have property", nextToken)
////                }
//                else -> throw PositionalException("this token is not expected", nextToken.left)
//            }
//            nextToken = nextToken.right
//        }
//        when (nextToken.symbol) {
//            "parent" -> {
//                parent = parent!!.parent
//                    ?: throw PositionalException("no parent in root class", nextToken.left)
//            }
//            "(IDENT)" -> {
//                val assignment = current.parent!!.assignments.find { it.name == nextToken.value }
//                if (assignment != null) {
//                    stack.add(assignment)
//                    return
//                } else if (parent!!.symbolTable.getVariableOrNull(nextToken.left) != null)
//                    parent = parent.symbolTable.getVariable(nextToken.left) as Type
//                else if (globalTable.getVariableOrNull(nextToken) != null)
//                    parent = globalTable.getVariable(nextToken) as Type
//                else throw PositionalException("no such identifier found", nextToken)
//            }
//            "(" -> {
//            }
//            else -> throw PositionalException("this token is not expected", nextToken.left)
//        }
//        processPrimitiveProperty()
//    }
//
//    private fun processPrimitiveProperty() {}
//
//    /**
//     * IDENT
//     * .
//     * ( cannot happen
//     */
//    private fun processChain(link: Token, current: Assignment, stack: Stack<Assignment>) {
////        var parent = current.parent
////
////        val notResolved = parent.assignments.find { it.name == link.value }
////        if (notResolved != null) {
////            // needed for resolve next
////            stack.push(notResolved)
////            return
////        } else if (parent.symbolTable.variables[link.value] != null) {
////            val smth = parent.symbolTable.variables[link.value]
////            // take value from existing node
////            if (smth is Primitive) {
////                current.replaceFirst(smth.value)
////                // stack.push(current)
////                return
////                // go one node deeper on chain
////            } else {
////                parent = smth as Type
////                linkToken = linkToken.children[1]
////            }
////        } else if (linkToken.children[0].value == "parent") {
////            parent = parent.parent!!
////            linkToken = linkToken.children[1]
////        } else throw PositionalException(
////            "no property with name ${link.value} in ${parent.name}",
////            current.token
////        )
//    }
//}
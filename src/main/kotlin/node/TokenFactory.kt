package node

import lexer.Parser
import lexer.PositionalException
import table.SymbolTable
import node.invocation.Call
import node.invocation.Constructor
import node.invocation.Invocation
import node.operator.ArithmeticOperator
import node.operator.Operator
import node.operator.TypeOperator
import node.statement.Assignment

object TokenFactory {
    private val nonArithmeticOperators = listOf("+", "==", "!=")
    private val arithmeticOperators = listOf("-", "*", "/", "%", ">=", "<=", ">", "<", "!", "&&", "||")
    private val wordOperators = listOf("is")

    fun createWordToken(
        symbol: String,
        value: String,
        position: Pair<Int, Int>,
        bindingPower: Int,
        nud: ((node: Node, parser: Parser) -> Node)?,
        led: ((node: Node, parser: Parser, node2: Node) -> Node)?,
        std: ((node: Node, parser: Parser) -> Node)?
    ): Node {
        return when (symbol) {
            in wordOperators -> TypeOperator(symbol, value, position, bindingPower, nud, led, std)
            else -> Identifier(symbol, value, position, bindingPower, nud, led, std)
        }
    }

    fun copy(node: Node, childrenStart: Int = 0, childrenNumber: Int = node.children.size): Node {
        // TODO check that children of token shouldn't be copied
        if (node is Assignment) {
            val res = Assignment(
                node.symbol,
                node.value,
                node.position,
                node.bindingPower,
                node.nud,
                node.led,
                node.std,
                node.children
            )
            res.isProperty = node.isProperty
            return res
        }
        // used for Link
        return node::class.constructors.toMutableList()[1].call(
            node.symbol,
            node.value,
            node.position,
            node.bindingPower,
            node.nud,
            node.led,
            node.std,
            node.children.subList(childrenStart, childrenStart + childrenNumber)
        )
    }

    fun createOperator(
        symbol: String,
        value: String,
        position: Pair<Int, Int>,
        bindingPower: Int,
        nud: ((node: Node, parser: Parser) -> Node)?,
        led: ((node: Node, parser: Parser, node2: Node) -> Node)?,
        std: ((node: Node, parser: Parser) -> Node)?
    ): Node {
        return when (symbol) {
            "!is" -> TypeOperator(symbol, value, position, bindingPower, nud, led, std)
            "(" -> Invocation(symbol, value, position, bindingPower, nud, led, std)
            "." -> Link(("(LINK)"), value, position, bindingPower, nud, led, std)
            "=" -> Assignment("(ASSIGNMENT)", value, position, bindingPower, nud, led, std)
            ";", "\n", "\r\n", "\r" -> Node("(SEP)", value, position, bindingPower, nud, led, std)
            in nonArithmeticOperators -> Operator(symbol, value, position, bindingPower, nud, led, std)
            in arithmeticOperators -> ArithmeticOperator(symbol, value, position, bindingPower, nud, led, std)
            else -> Node(symbol, value, position, bindingPower, nud, led, std)
        }
    }

    fun createSpecificInvocation(
        nodeIdentifier: Node,
        symbolTable: SymbolTable,
        upperNode: Node,
        index: Int
    ): Invocation {
        if (symbolTable.getFunctionOrNull(Call(nodeIdentifier)) != null)
            upperNode.children[index] = Call(nodeIdentifier)
        else if (symbolTable.getTypeOrNull(nodeIdentifier.left) != null)
            upperNode.children[index] = Constructor(nodeIdentifier)
        else throw PositionalException("No class and function found", nodeIdentifier.left)
        return upperNode.children[index] as Invocation
    }

    fun changeInvocationOnSecondPositionInLink(symbolTable: SymbolTable, link: Link): Invocation {
        // a weak check
        if (symbolTable.getAssignmentOrNull(link.left.value) != null
            || link.left.value == "this"
            || symbolTable.getVariableOrNull(link.left.value) != null
            || symbolTable.getObjectOrNull(link.left) != null
        ) {
            link.children[1] = Call(link.right)
            return link.children[1] as Call
        }
        val fileTable = symbolTable.getImportOrNull(link.left.value) ?: throw PositionalException(
            "Variable or import not found",
            link.left
        )
        if (fileTable.getFunctionOrNull(Call(link.right)) != null)
            link.children[1] = Call(link.right)
        else if (fileTable.getTypeOrNull(link.right.left.value) != null)
            link.children[1] = Constructor(link.right)
        else throw PositionalException("No class and function found in `${fileTable.fileName}`", link.right)
        return link.children[1] as Invocation
    }
}
